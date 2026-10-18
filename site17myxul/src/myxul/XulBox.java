package myxul;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * XUL's box: children in a row (<hbox>) or a column (<vbox>), sharing spare
 * room by their flex weights. JavaFX's HBox and VBox can only make children
 * grow equally; this pane hands out space in proportion, so flex="1" next to
 * flex="3" gets a quarter and three quarters.
 *
 * align (across the box): stretch (XUL's default), start, center, end.
 * pack (along the box, when nothing flexes): start (default), center, end.
 */
public class XulBox extends Pane {

	private static final String FLEX = "xul-flex";
	private static final String MARGIN = "xul-margin";
	private static final String LIMITED = "xul-limited";

	private final Orientation orientation;
	private double spacing;
	private String align = "stretch";
	private String pack = "start";

	public XulBox(Orientation orientation) {
		this.orientation = orientation;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public void setSpacing(double spacing) {
		this.spacing = spacing;
		requestLayout();
	}

	public void setAlign(String align) {
		this.align = align == null ? "stretch" : align;
		requestLayout();
	}

	public void setPack(String pack) {
		this.pack = pack == null ? "start" : pack;
		requestLayout();
	}

	/** A child's flex weight: 0 keeps its preferred size. */
	public static void setFlex(Node child, double flex) {
		child.getProperties().put(FLEX, flex);
		if (child.getParent() != null) {
			child.getParent().requestLayout();
		}
	}

	public static double getFlex(Node child) {
		Object flex = child.getProperties().get(FLEX);
		return flex instanceof Double ? (Double) flex : 0;
	}

	public static void setMargin(Node child, Insets margin) {
		child.getProperties().put(MARGIN, margin);
	}

	/**
	 * Marks a child whose maxwidth or maxheight was set in the XUL. Other
	 * children grow past their controls' own maximums when they flex or
	 * stretch, as in XUL (a JavaFX Button's maximum is its preferred size).
	 */
	public static void setLimited(Node child) {
		child.getProperties().put(LIMITED, Boolean.TRUE);
	}

	private static boolean limited(Node child) {
		return child.getProperties().containsKey(LIMITED);
	}

	private static Insets margin(Node child) {
		Object margin = child.getProperties().get(MARGIN);
		return margin instanceof Insets ? (Insets) margin : Insets.EMPTY;
	}

	private boolean horizontal() {
		return orientation == Orientation.HORIZONTAL;
	}

	// Sizes along (main) and across (cross) the box, including margins.

	private double mainPref(Node c, double cross) {
		Insets m = margin(c);
		double pref = horizontal() ? c.prefWidth(cross) : c.prefHeight(cross);
		if (limited(c)) {
			pref = Math.min(pref, horizontal() ? c.maxWidth(cross) : c.maxHeight(cross));
		}
		return horizontal() ? pref + m.getLeft() + m.getRight() : pref + m.getTop() + m.getBottom();
	}

	private double mainMin(Node c, double cross) {
		Insets m = margin(c);
		return horizontal() ? c.minWidth(cross) + m.getLeft() + m.getRight() : c.minHeight(cross) + m.getTop() + m.getBottom();
	}

	private double mainMax(Node c, double cross) {
		Insets m = margin(c);
		return horizontal() ? c.maxWidth(cross) + m.getLeft() + m.getRight() : c.maxHeight(cross) + m.getTop() + m.getBottom();
	}

	private double crossPref(Node c) {
		Insets m = margin(c);
		return horizontal() ? c.prefHeight(-1) + m.getTop() + m.getBottom() : c.prefWidth(-1) + m.getLeft() + m.getRight();
	}

	private double crossMin(Node c) {
		Insets m = margin(c);
		return horizontal() ? c.minHeight(-1) + m.getTop() + m.getBottom() : c.minWidth(-1) + m.getLeft() + m.getRight();
	}

	private double gaps() {
		int n = 0;
		for (Node c : getManagedChildren()) {
			n++;
		}
		return n > 1 ? spacing * (n - 1) : 0;
	}

	@Override
	protected double computePrefWidth(double height) {
		Insets in = getInsets();
		double size = 0;
		for (Node c : getManagedChildren()) {
			size = horizontal() ? size + mainPref(c, -1) : Math.max(size, crossPref(c));
		}
		return in.getLeft() + in.getRight() + size + (horizontal() ? gaps() : 0);
	}

	@Override
	protected double computePrefHeight(double width) {
		Insets in = getInsets();
		double size = 0;
		for (Node c : getManagedChildren()) {
			size = horizontal() ? Math.max(size, crossPref(c)) : size + mainPref(c, -1);
		}
		return in.getTop() + in.getBottom() + size + (horizontal() ? 0 : gaps());
	}

	@Override
	protected double computeMinWidth(double height) {
		Insets in = getInsets();
		double size = 0;
		for (Node c : getManagedChildren()) {
			size = horizontal() ? size + mainMin(c, -1) : Math.max(size, crossMin(c));
		}
		return in.getLeft() + in.getRight() + size + (horizontal() ? gaps() : 0);
	}

	@Override
	protected double computeMinHeight(double width) {
		Insets in = getInsets();
		double size = 0;
		for (Node c : getManagedChildren()) {
			size = horizontal() ? Math.max(size, crossMin(c)) : size + mainMin(c, -1);
		}
		return in.getTop() + in.getBottom() + size + (horizontal() ? 0 : gaps());
	}

	@Override
	protected void layoutChildren() {
		Insets in = getInsets();
		java.util.List<Node> kids = getManagedChildren();
		int n = kids.size();
		if (n == 0) {
			return;
		}
		double crossSpace = horizontal() ? getHeight() - in.getTop() - in.getBottom() : getWidth() - in.getLeft() - in.getRight();
		double mainSpace = (horizontal() ? getWidth() - in.getLeft() - in.getRight() : getHeight() - in.getTop() - in.getBottom()) - gaps();

		// Each child's cross size first, since a wrapped label's height depends on its width.
		double[] cross = new double[n];
		for (int i = 0; i < n; i++) {
			Node c = kids.get(i);
			double pref = crossPref(c);
			double max = limited(c) ? (horizontal() ? c.maxHeight(-1) : c.maxWidth(-1)) : Double.MAX_VALUE;
			cross[i] = "stretch".equals(align) ? Math.min(crossSpace, Math.max(max, pref)) : Math.min(crossSpace, pref);
			cross[i] = Math.max(cross[i], Math.min(crossMin(c), crossSpace));
		}

		// Then sizes along the box: preferred sizes, then spare room (or shortage) shared by flex.
		double[] main = new double[n];
		double[] min = new double[n];
		double[] max = new double[n];
		double[] flex = new double[n];
		double used = 0;
		for (int i = 0; i < n; i++) {
			Node c = kids.get(i);
			main[i] = mainPref(c, cross[i]);
			flex[i] = getFlex(c);
			if (limited(c)) {   // maxwidth/maxheight: never bigger, not even at its preferred size
				max[i] = mainMax(c, cross[i]);
				main[i] = Math.min(main[i], max[i]);
			} else {
				max[i] = flex[i] > 0 ? Double.MAX_VALUE : Math.max(mainMax(c, cross[i]), main[i]);
			}
			min[i] = Math.min(mainMin(c, cross[i]), main[i]);
			used += main[i];
		}
		double extra = mainSpace - used;
		boolean[] done = new boolean[n];
		for (int round = 0; round < n && Math.abs(extra) > 0.5; round++) {
			double weights = 0;
			for (int i = 0; i < n; i++) {
				if (!done[i] && flex[i] > 0) {
					weights += flex[i];
				}
			}
			if (weights == 0) {
				break;
			}
			double handedOut = 0;
			for (int i = 0; i < n; i++) {
				if (done[i] || flex[i] == 0) {
					continue;
				}
				double share = extra * flex[i] / weights;
				double wanted = main[i] + share;
				double limited = Math.max(min[i], Math.min(max[i], wanted));
				if (limited != wanted) {
					done[i] = true;   // hit its limit; the rest is shared again among the others
				}
				handedOut += limited - main[i];
				main[i] = limited;
			}
			extra -= handedOut;
			if (handedOut == 0) {
				break;
			}
		}
		if (extra < -0.5) {   // still too big: shrink non-flex children towards their minimum sizes
			for (int i = n - 1; i >= 0 && extra < -0.5; i--) {
				double give = Math.min(main[i] - min[i], -extra);
				main[i] -= give;
				extra += give;
			}
		}

		double position = horizontal() ? in.getLeft() : in.getTop();
		if (extra > 0.5) {
			if ("center".equals(pack)) {
				position += extra / 2;
			} else if ("end".equals(pack)) {
				position += extra;
			}
		}
		for (int i = 0; i < n; i++) {
			Node c = kids.get(i);
			Insets m = margin(c);
			double crossStart = horizontal() ? in.getTop() : in.getLeft();
			double free = crossSpace - cross[i];
			if ("center".equals(align)) {
				crossStart += free / 2;
			} else if ("end".equals(align)) {
				crossStart += free;
			}
			if (horizontal()) {
				c.resizeRelocate(snapPosition(position + m.getLeft()), snapPosition(crossStart + m.getTop()),
						snapSize(main[i] - m.getLeft() - m.getRight()), snapSize(cross[i] - m.getTop() - m.getBottom()));
			} else {
				c.resizeRelocate(snapPosition(crossStart + m.getLeft()), snapPosition(position + m.getTop()),
						snapSize(cross[i] - m.getLeft() - m.getRight()), snapSize(main[i] - m.getTop() - m.getBottom()));
			}
			position += main[i] + spacing;
		}
	}
}
