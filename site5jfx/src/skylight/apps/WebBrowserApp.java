package skylight.apps;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import skylight.App;

/** Web Browser: JavaFX's built-in WebView, with back, forward, reload and an address bar. */
public final class WebBrowserApp extends App {

	private static final String START_PAGE =
			"<html><body style='font-family: sans-serif; color: #444; margin: 40px'>"
			+ "<h2>Skylight Web Browser</h2><p>Type an address above and press Enter.</p></body></html>";

	private WebEngine engine;

	@Override
	protected Parent build() {
		WebView view = new WebView();
		engine = view.getEngine();

		TextField address = new TextField();
		address.setPromptText("https://");
		HBox.setHgrow(address, Priority.ALWAYS);
		address.setOnAction(e -> {
			String url = address.getText().trim();
			engine.load(url.matches("(?i)[a-z]+:.*") ? url : "http://" + url);
		});
		// Keep the address bar in step with links the user follows.
		engine.locationProperty().addListener((property, old, location) -> address.setText(location));

		Button back = new Button("◀");
		back.setOnAction(e -> go(-1));
		Button forward = new Button("▶");
		forward.setOnAction(e -> go(1));
		Button reload = new Button("↻");
		reload.setOnAction(e -> engine.reload());

		engine.loadContent(START_PAGE);
		BorderPane root = new BorderPane(view);
		root.setTop(new ToolBar(back, forward, reload, address));
		return root;
	}

	private void go(int step) {
		WebHistory history = engine.getHistory();
		int target = history.getCurrentIndex() + step;
		if (target >= 0 && target < history.getEntries().size()) {
			history.go(step);
		}
	}
}
