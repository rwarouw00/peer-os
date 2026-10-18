package myxul.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import myxul.XulWindow;

/**
 * Notifications as Skylight OS shows them (scripts call it Notifications):
 * a small window at the top right of the screen with a header and a body,
 * which closes itself after the time set in Settings, or when its × is
 * clicked. Several stack downwards.
 */
public class NotificationService implements XulService {

	private static final double WIDTH = 300;
	private static final double GAP = 8;
	private static final List<Stage> SHOWING = new ArrayList<>();
	private static final List<String> POSTED = new ArrayList<>();

	public NotificationService(XulWindow window) {
	}

	public void post(String header) {
		post(header, "");
	}

	/** Shows a notification for the time set in Settings. */
	public void post(String header, String body) {
		POSTED.add(header + (body == null || body.isEmpty() ? "" : ": " + body));
		Label title = new Label(header);
		title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
		Label text = new Label(body == null ? "" : body);
		text.setWrapText(true);
		Button close = new Button("×");
		close.setStyle("-fx-padding: 0 6 0 6; -fx-background-color: transparent; -fx-font-size: 14px;");
		BorderPane top = new BorderPane();
		top.setLeft(title);
		top.setRight(close);
		BorderPane.setAlignment(title, Pos.CENTER_LEFT);
		VBox box = new VBox(4, top, text);
		box.setPadding(new Insets(8, 10, 10, 12));
		box.setPrefWidth(WIDTH);
		box.setStyle("-fx-background-color: #fdfdfd; -fx-border-color: #b8b8b8; -fx-border-width: 1;");
		if (body == null || body.isEmpty()) {
			box.getChildren().remove(text);
		}
		Stage toast = new Stage(StageStyle.UNDECORATED);
		toast.setAlwaysOnTop(true);
		toast.setScene(new Scene(box));
		toast.setTitle("Notification: " + header);
		close.setOnAction(e -> hide(toast));
		PauseTransition wait = new PauseTransition(Duration.millis(SettingsService.notificationTime()));
		wait.setOnFinished(e -> hide(toast));
		SHOWING.add(toast);
		toast.show();
		restack();
		wait.play();
	}

	private static void hide(Stage toast) {
		toast.close();
		SHOWING.remove(toast);
		restack();
	}

	private static void restack() {
		Rectangle2D screen = Screen.getPrimary().getVisualBounds();
		double y = screen.getMinY() + GAP;
		for (Stage toast : SHOWING) {
			toast.setX(screen.getMaxX() - WIDTH - GAP);
			toast.setY(y);
			y += toast.getHeight() + GAP;
		}
	}

	/** How many notifications are on screen now (for tests). */
	public static int showing() {
		return SHOWING.size();
	}

	/** Every notification posted since myxul started, "header: body" (for tests). */
	public static List<String> posted() {
		return Collections.unmodifiableList(POSTED);
	}
}
