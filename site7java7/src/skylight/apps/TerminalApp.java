package skylight.apps;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import skylight.App;

/**
 * Terminal: runs JavaScript with Java's built-in script engine (Rhino on Java 7,
 * Nashorn on Java 8). The variable app is this app, so app.postNotification('Hi')
 * and the rest of the App API work.
 */
public final class TerminalApp extends App {

	private final TextArea code = new TextArea();
	private final TextArea output = new TextArea();
	private ScriptEngine engine;

	@Override
	protected Parent build() {
		code.getStyleClass().add("terminal-code");
		output.getStyleClass().add("terminal-output");
		output.setEditable(false);

		Button run = new Button("Run");
		run.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				run();
			}
		});
		Button clear = new Button("Clear output");
		clear.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				output.clear();
			}
		});
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		ToolBar toolbar = new ToolBar(run, clear, spacer, new Label("JavaScript. app is this app."));

		SplitPane panes = new SplitPane();
		panes.getItems().addAll(code, output);
		panes.setOrientation(Orientation.VERTICAL);
		panes.setDividerPositions(0.6);

		BorderPane root = new BorderPane();
		root.setCenter(panes);
		root.setTop(toolbar);
		return root;
	}

	/** Runs the code and shows what it printed and returned, or the error. */
	void run() {
		if (engine == null) {
			engine = new ScriptEngineManager().getEngineByName("JavaScript");
			if (engine == null) {
				output.appendText("Error: this Java has no JavaScript engine.\n");
				return;
			}
			engine.put("app", this);
		}
		StringWriter printed = new StringWriter();
		engine.getContext().setWriter(new PrintWriter(printed, true));
		String text;
		try {
			Object result = engine.eval(code.getText());
			text = printed.toString() + (result == null ? "" : "=> " + result + "\n");
		} catch (ScriptException e) {
			text = printed.toString() + "Error: " + e.getMessage() + "\n";
		} catch (RuntimeException e) {
			text = printed.toString() + "Error: " + e + "\n";
		}
		output.appendText(text.isEmpty() ? "(done)\n" : text);
	}
}
