package skylight;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import houston.Status;
import houston.StatusListener;
import luxor.XulAction;
import luxor.XulManager;
import luxor.core.XulLoader;
import luxor.core.XulTagInfoMap;
import luxor.spi.ChromeResourceLoader;
import luxor.spi.JComponentResolver;
import luxor.spi.NContainer;
import luxor.spi.NInput;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Skylight's bridge to Luxor XUL. Every window's contents are a .xul file: its
 * first box becomes the window, controls are found by their id, and each
 * command="name" calls back into Skylight (for the desktop's own windows) or
 * into the app's script (for apps).
 */
final class Xul {

	/** A loaded .xul file: its Swing component, and its controls by id. */
	static final class View {
		final JComponent root;
		final Map<String, Object> ids;

		View(JComponent root, Map<String, Object> ids) {
			this.root = root;
			this.ids = ids;
		}

		/** The control with this id: a JButton, JLabel, JTextField, JTextArea, ... */
		@SuppressWarnings("unchecked")
		<T> T get(String id) {
			return (T) ids.get(id);
		}
	}

	/** A .xul file that couldn't be loaded, with Luxor's messages. */
	static final class XulException extends Exception {
		XulException(String message) {
			super(message);
		}
	}

	private static XulManager manager;
	private static List<String> problems;   // Luxor's errors and warnings while a file loads

	private Xul() {
	}

	/** Sets Luxor up to read files from the Skylight folder. Call once, before anything else. */
	static void init() {
		manager = XulManager.getXulManager();
		manager.setResourceLoader(new ChromeResourceLoader() {
			public List getBootstrapEntries() {
				return Collections.EMPTY_LIST;   // Skylight loads each file when it's needed
			}

			public List getConfigEntries() {
				return Collections.EMPTY_LIST;
			}

			public Object getConfigData(String key) {
				return null;
			}

			public InputStream getResourceAsStream(String name) {
				try {
					return new FileInputStream(new File(Storage.HOME.toFile(), name));
				} catch (IOException e) {
					return null;
				}
			}

			public URL getResourceAsUrl(String name) {
				File file = new File(Storage.HOME.toFile(), name);
				try {
					return file.exists() ? file.toURI().toURL() : null;
				} catch (MalformedURLException e) {
					return null;
				}
			}
		});
		// Luxor reports problems through houston.Status; keep those that happen while a file loads.
		Status.addListener(new StatusListener() {
			public void error(String message) {
				problem(message);
			}

			public void fatal(String message) {
				problem(message);
			}

			public void warning(String message) {
				problem(message);
			}

			public void hint(String message) {
			}

			public void info(String message) {
			}

			public void info(int level, String message) {
			}
		});
	}

	private static void problem(String message) {
		if (problems != null) {
			problems.add(message.replace("*** xul ", ""));
		} else {
			System.err.println(message);
		}
	}

	/**
	 * Loads a .xul file (a path in the Skylight folder) and builds its first box.
	 * Each command in the file calls onCommand with the command's name; each
	 * <gadget id="..."/> becomes an empty JPanel that code can fill.
	 */
	static View load(String file, Consumer<String> onCommand) throws XulException {
		// Read the file ourselves first: Luxor only logs XML mistakes, and we need
		// the first box's id, every id and every command.
		Element root;
		try {
			Document doc = new SAXBuilder().build(new File(Storage.HOME.toFile(), file));
			root = doc.getRootElement();
		} catch (JDOMException e) {
			// "Error on line 5 of document file:/.../app.xul: ..." -> "Error on line 5: ..."
			throw new XulException(file + ": " + e.getMessage().replaceFirst(" of document \\S+: ", ": "));
		}
		String boxId = null;
		for (Iterator it = root.getChildren().iterator(); it.hasNext() && boxId == null;) {
			Element child = (Element) it.next();
			if (child.getName().endsWith("box")) {
				boxId = child.getAttributeValue("id", "");
			}
		}
		if (!"xul".equals(root.getName()) || boxId == null) {
			throw new XulException(file + ": expected <xul> with a <vbox> or <hbox> inside it.");
		}
		if (boxId.isEmpty()) {
			throw new XulException(file + ": the window's <vbox> or <hbox> needs an id.");
		}
		Set<String> ids = new LinkedHashSet<>();
		Set<String> commands = new LinkedHashSet<>();
		collect(root, ids, commands);

		// Each window registers its own actions just before its controls are made:
		// Luxor gives every control the action registered under its command now,
		// so two windows of the same app each keep their own.
		for (String command : commands) {
			new XulAction(command) {
				public void execute() {
					onCommand.accept(command);
				}
			};
		}

		Map<String, JPanel> gadgets = new LinkedHashMap<>();
		problems = new ArrayList<>();
		NContainer container;
		try {
			new XulLoader(manager.getDefMap(), manager, XulTagInfoMap.getXulTagInfoMap()).load(file);
			container = manager.createNContainerFromBox(boxId, new JComponentResolver() {
				public JComponent lookupJComponent(String id) {
					JPanel gadget = new JPanel(new BorderLayout());
					gadget.setOpaque(false);
					gadget.setName(id);
					gadgets.put(id, gadget);
					return gadget;
				}
			});
			if (!problems.isEmpty() || container == null) {
				throw new XulException(file + ": " + String.join("; ", problems));
			}
		} finally {
			problems = null;
		}

		// Controls by id: Luxor names most Swing components after their id;
		// text areas are found through Luxor's own index of inputs.
		Map<String, Object> found = new LinkedHashMap<>();
		findNamed(container.getJComponent(), found);
		for (String id : ids) {
			if (!found.containsKey(id)) {
				NInput input = container.lookupNInput(id);
				if (input != null) {
					found.put(id, unwrap(input.getJComponent()));
				}
			}
		}
		found.putAll(gadgets);
		return new View(container.getJComponent(), found);
	}

	private static void collect(Element element, Set<String> ids, Set<String> commands) {
		String id = element.getAttributeValue("id");
		if (id != null && !"xul".equals(element.getName())) {
			ids.add(id);
		}
		String command = element.getAttributeValue("command");
		if (command != null) {
			commands.add(command);
		}
		for (Iterator it = element.getChildren().iterator(); it.hasNext();) {
			collect((Element) it.next(), ids, commands);
		}
	}

	private static void findNamed(Component component, Map<String, Object> found) {
		String name = component.getName();
		if (name != null && !"anonymous".equals(name) && !found.containsKey(name)) {
			found.put(name, unwrap(component));
		}
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				findNamed(child, found);
			}
		}
	}

	private static Object unwrap(Component component) {
		return component instanceof JScrollPane ? ((JScrollPane) component).getViewport().getView() : component;
	}
}
