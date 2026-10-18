package myxul;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Starts myxul: java -jar myxul.jar [file.xul [arguments...]]. With no file,
 * opens ui/launcher.xul from the folder myxul.jar is in. Arguments after the file become the window's
 * window.arguments (Leafpad XUL opens the file named in its first).
 */
public class Main extends Application {

	/** The folder myxul.jar is in (or the current folder, when running from classes). */
	public static Path home() {
		try {
			Path jar = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			return jar.toString().endsWith(".jar") ? jar.getParent() : Paths.get("");
		} catch (Exception e) {
			return Paths.get("");
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage unused) {
		List<String> args = getParameters().getRaw();
		Path file = args.isEmpty() ? home().resolve("ui/launcher.xul") : Paths.get(args.get(0));
		XulEngine engine = new XulEngine();
		if (engine.openWindow(file, args.isEmpty() ? args : args.subList(1, args.size())) == null) {
			Platform.exit();
		}
	}
}
