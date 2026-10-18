package myxul;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;

/**
 * XUL's <canvas>: a javafx.scene.canvas.Canvas inside a resizable region, so
 * it can flex like any other element (a Canvas on its own has a fixed size).
 * The canvas is always as big as the region; listen to its width and height
 * to repaint after a resize.
 */
public class XulCanvas extends Region {

	private final Canvas canvas = new Canvas();

	public XulCanvas() {
		getChildren().add(canvas);
		setPrefSize(300, 150);   // a web canvas's default size
		setFocusTraversable(true);
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public GraphicsContext getGraphicsContext2D() {
		return canvas.getGraphicsContext2D();
	}

	@Override
	protected void layoutChildren() {
		double w = getWidth() - snappedLeftInset() - snappedRightInset();
		double h = getHeight() - snappedTopInset() - snappedBottomInset();
		canvas.relocate(snappedLeftInset(), snappedTopInset());
		canvas.setWidth(Math.max(0, w));
		canvas.setHeight(Math.max(0, h));
	}
}
