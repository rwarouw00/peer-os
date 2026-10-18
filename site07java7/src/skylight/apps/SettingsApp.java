package skylight.apps;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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
			final String path = "images/backgrounds/" + i + ".jpg";
			ImageView thumbnail = new ImageView(new Image(Storage.HOME.resolve(path).toUri().toString(), 128, 80, false, true));
			final ToggleButton choice = new ToggleButton(null, thumbnail);
			choice.setToggleGroup(group);
			choice.setSelected(path.equals(settings.background));
			choice.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					choice.setSelected(true);   // a choice stays chosen
					setBackground(path);
				}
			});
			backgrounds.getChildren().add(choice);
		}

		// JavaFX 2.2 has no number spinner; a slider that snaps to whole seconds does the job.
		final Slider seconds = new Slider(1, 60, settings.notificationTime / 1000);
		seconds.setMajorTickUnit(1);
		seconds.setMinorTickCount(0);
		seconds.setSnapToTicks(true);
		seconds.setPrefWidth(160);
		final Label secondsText = new Label();
		secondsText.setMinWidth(70);
		secondsText.setText(Math.round(seconds.getValue()) + " seconds");
		seconds.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> property, Number old, Number value) {
				secondsText.setText(Math.round(value.doubleValue()) + " seconds");
			}
		});
		Button save = new Button("Save");
		save.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Settings changed = getSettings();
				long chosen = Math.round(seconds.getValue());
				changed.notificationTime = (int) chosen * 1000;
				if (saveSettings(changed)) {
					postNotification("Settings", "Notifications now stay for " + chosen + " seconds.");
				}
			}
		});
		HBox time = new HBox(8);
		time.getChildren().addAll(new Label("Show notifications for"), seconds, secondsText, save);
		time.setAlignment(Pos.CENTER_LEFT);

		VBox root = new VBox(10);
		root.getChildren().addAll(heading("Background"), backgrounds, heading("Notifications"), time);
		root.setPadding(new Insets(14));
		return root;
	}

	private static Label heading(String text) {
		Label label = new Label(text);
		label.getStyleClass().add("heading");
		return label;
	}
}
