package myxul;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

/**
 * The dialogs behind window.alert(), confirm() and prompt(), and the error
 * dialog for mistakes in XUL files and scripts. An embedding program (or a
 * test) can give the engine its own.
 */
public interface XulDialogs {

	void alert(XulWindow window, String message);

	boolean confirm(XulWindow window, String message);

	/** The text typed, or null if the user cancelled. */
	String prompt(XulWindow window, String message, String value);

	/** A mistake in a XUL file or script. The window is null if it never opened. */
	void error(XulWindow window, String message);

	/** JavaFX's own dialogs, owned by the window they're for. */
	XulDialogs JAVAFX = new XulDialogs() {
		@Override
		public void alert(XulWindow window, String message) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
			alert.setHeaderText(null);
			alert.setTitle(window == null ? "myxul" : window.getTitle());
			owner(alert, window);
			alert.showAndWait();
		}

		@Override
		public boolean confirm(XulWindow window, String message) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
			alert.setHeaderText(null);
			alert.setTitle(window == null ? "myxul" : window.getTitle());
			owner(alert, window);
			Optional<ButtonType> answer = alert.showAndWait();
			return answer.isPresent() && answer.get() == ButtonType.OK;
		}

		@Override
		public String prompt(XulWindow window, String message, String value) {
			TextInputDialog dialog = new TextInputDialog(value);
			dialog.setHeaderText(null);
			dialog.setContentText(message);
			dialog.setTitle(window == null ? "myxul" : window.getTitle());
			if (window != null && window.stage.isShowing()) {
				dialog.initOwner(window.stage);
			}
			return dialog.showAndWait().orElse(null);
		}

		@Override
		public void error(XulWindow window, String message) {
			Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
			alert.setHeaderText(window == null ? "This window can't be opened" : "Script error");
			alert.setTitle("myxul");
			owner(alert, window);
			alert.showAndWait();
		}

		private void owner(Alert alert, XulWindow window) {
			if (window != null && window.stage.isShowing()) {
				alert.initOwner(window.stage);
			}
		}
	};
}
