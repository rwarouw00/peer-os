package skylight;

import java.io.IOException;
import javafx.scene.Parent;

/**
 * An app made of FXML alone: the file named by fxml= in app.properties. Its
 * scripts (fx:script, in JavaScript) reach the App API through the variable app.
 */
final class FxmlApp extends App {

	@Override
	protected Parent build() {
		try {
			return loadFxml(manifest.getProperty("fxml"));
		} catch (IOException e) {
			throw new IllegalStateException(path + manifest.getProperty("fxml") + ": " + e.getMessage(), e);
		}
	}
}
