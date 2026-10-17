package skylight.apps;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import skylight.App;
import skylight.Settings;
import skylight.Storage;

/** Settings: desktop background, and how long notifications stay on screen. */
public final class SettingsApp extends App {

	@Override
	protected Parent build() {
		Settings settings = getSettings();

		ToggleGroup group = new ToggleGroup();
		TilePane backgrounds = new TilePane(8, 8);
		backgrounds.setPrefColumns(3);
		for (int i = 1; i <= 6; i++) {
			String path = "images/backgrounds/" + i + ".jpg";
			ImageView thumbnail = new ImageView(new Image(Storage.HOME.resolve(path).toUri().toString(), 128, 80, false, true));
			ToggleButton choice = new ToggleButton(null, thumbnail);
			choice.setToggleGroup(group);
			choice.setSelected(path.equals(settings.background));
			choice.setOnAction(e -> {
				choice.setSelected(true);   // a choice stays chosen
				setBackground(path);
			});
			backgrounds.getChildren().add(choice);
		}

		Spinner<Integer> seconds = new Spinner<>(1, 60, settings.notificationTime / 1000);
		seconds.setPrefWidth(80);
		Button save = new Button("Save");
		save.setOnAction(e -> {
			Settings changed = getSettings();
			changed.notificationTime = seconds.getValue() * 1000;
			if (saveSettings(changed)) {
				postNotification("Settings", "Notifications now stay for " + seconds.getValue() + " seconds.");
			}
		});
		HBox time = new HBox(8, new Label("Show notifications for"), seconds, new Label("seconds"), save);
		time.setAlignment(Pos.CENTER_LEFT);

		VBox root = new VBox(10, heading("Background"), backgrounds, heading("Notifications"), time);
		root.setPadding(new Insets(14));
		return root;
	}

	private static Label heading(String text) {
		Label label = new Label(text);
		label.getStyleClass().add("heading");
		return label;
	}
}
