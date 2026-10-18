package myxul;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads a .xul file into a tree of XulElements, each knowing its line. Uses
 * SAX rather than a DOM so that line numbers are kept, and refuses DOCTYPEs
 * and external entities, so a XUL file can't pull other files into the UI.
 */
final class XulParser {

	/** XUL's namespace. Elements in other namespaces (or none) are read by their local name too. */
	static final String XUL_NS = "http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul";

	/** A parsed file: its root element and its processing instructions (<?xul-service ...?>). */
	static final class Parsed {
		final XulElement root;
		final List<String[]> instructions;   // {target, data, line}

		Parsed(XulElement root, List<String[]> instructions) {
			this.root = root;
			this.instructions = instructions;
		}
	}

	static Parsed parse(Path file) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (ParserConfigurationException | SAXException e) {
			throw new IllegalStateException("The XML parser can't be made safe: " + e, e);
		}
		Handler handler = new Handler(file);
		try (InputStream in = Files.newInputStream(file)) {
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, handler);
		} catch (NoSuchFileException e) {
			throw new XulException(file, 0, "There is no such file.");
		} catch (SAXParseException e) {
			String message = e.getMessage();
			if (message != null && message.contains("DOCTYPE")) {
				message = "DOCTYPE declarations aren't allowed in XUL files here (they can pull in other files).";
			}
			throw new XulException(file, e.getLineNumber(), "Not well-formed XML: " + message);
		} catch (SAXException | ParserConfigurationException e) {
			throw new XulException(file, 0, "The file can't be read as XML: " + e.getMessage());
		} catch (IOException e) {
			throw new XulException(file, 0, "The file can't be read: " + e.getMessage());
		}
		if (handler.root == null) {
			throw new XulException(file, 0, "The file has no elements.");
		}
		return new Parsed(handler.root, handler.instructions);
	}

	private static final class Handler extends DefaultHandler {
		private final Path file;
		private final Deque<XulElement> open = new ArrayDeque<>();
		private final List<String[]> instructions = new ArrayList<>();
		private Locator locator;
		private XulElement root;

		Handler(Path file) {
			this.file = file;
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			String tag = localName == null || localName.isEmpty() ? qName : localName;
			XulElement element = new XulElement(tag, file, locator == null ? 0 : locator.getLineNumber());
			element.namespace = uri == null ? "" : uri;
			for (int i = 0; i < attrs.getLength(); i++) {
				String name = attrs.getLocalName(i);
				if (name == null || name.isEmpty()) {
					name = attrs.getQName(i);
				}
				element.attributes.put(name, attrs.getValue(i));
			}
			XulElement parent = open.peek();
			if (parent == null) {
				root = element;
			} else {
				element.parent = parent;
				parent.children.add(element);
			}
			open.push(element);
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			open.pop();
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			XulElement current = open.peek();
			if (current != null) {
				current.text += new String(ch, start, length);
			}
		}

		@Override
		public void processingInstruction(String target, String data) {
			instructions.add(new String[] { target, data, String.valueOf(locator == null ? 0 : locator.getLineNumber()) });
		}
	}

	private XulParser() {
	}
}
