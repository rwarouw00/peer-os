package skylight.apps;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import skylight.App;

/**
 * Terminal: runs JavaScript with Java 8's built-in Nashorn engine. The variable
 * app is this app, so app.postNotification('Hi') and the rest of the App API work.
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
		run.setDefaultButton(false);
		run.setOnAction(e -> run());
		Button clear = new Button("Clear output");
		clear.setOnAction(e -> output.clear());
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		ToolBar toolbar = new ToolBar(run, clear, spacer, new Label("JavaScript. app is this app."));

		SplitPane panes = new SplitPane(code, output);
		panes.setOrientation(Orientation.VERTICAL);
		panes.setDividerPositions(0.6);

		BorderPane root = new BorderPane(panes);
		root.setTop(toolbar);
		return root;
	}

	/** Runs the code and shows what it printed and returned, or the error. */
	void run() {
		if (engine == null) {
			engine = new ScriptEngineManager().getEngineByName("nashorn");
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
