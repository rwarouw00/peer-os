package skylight;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/** Starts Skylight OS: java -jar skylight.jar */
public final class Main extends Application {

	@Override
	public void start(Stage unused) {
		// Logging out closes every window; that mustn't end the program.
		Platform.setImplicitExit(false);
		new Desktop().showLogin();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
