package myxul.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import myxul.XulCanvas;
import myxul.XulWindow;

/**
 * The AbiWord XUL shell's document engine (scripts call it AbiCoreEngine): a
 * plain-text document of paragraphs, each with a style (Normal, Heading 1,
 * Heading 2), painted as Letter-sized pages on a XUL <canvas>.
 *
 * It is a shell, not a word processor: typing adds to the end of the
 * document (Backspace removes from the end), bold and italic apply to the
 * whole document, and there is no selection.
 */
public class PageView implements XulService {

	private static final double PAGE_WIDTH = 816;    // 8.5 inches at 96 pixels an inch
	private static final double PAGE_HEIGHT = 1056;  // 11 inches
	private static final double MARGIN = 96;         // 1 inch
	private static final double GAP = 24;

	private static final class Paragraph {
		final StringBuilder text = new StringBuilder();
		String style = "Normal";
	}

	private final List<Paragraph> paragraphs = new ArrayList<>();
	private XulCanvas canvas;
	private String fontFamily = "serif";
	private boolean bold;
	private boolean italic;
	private double fontSize = 16;
	private int pageCount = 1;
	private ScriptObjectMirror onChange;

	public PageView(XulWindow window) {
		paragraphs.add(new Paragraph());
	}

	/** Draws on this <canvas> (pass its .node), repainting when it's resized, and takes typing from it. */
	public void attach(Object node) {
		if (!(node instanceof XulCanvas)) {
			throw new IllegalArgumentException("attach() needs a <canvas> element's node");
		}
		canvas = (XulCanvas) node;
		canvas.getCanvas().widthProperty().addListener(o -> paint());
		canvas.getCanvas().heightProperty().addListener(o -> paint());
		canvas.setOnMouseClicked(e -> canvas.requestFocus());
		canvas.setOnKeyTyped(e -> {
			String ch = e.getCharacter();
			if (ch.isEmpty() || e.isControlDown() || e.isMetaDown() || e.isAltDown()) {
				return;
			}
			char c = ch.charAt(0);
			if (c == '\r' || c == '\n') {
				paragraphs.add(new Paragraph());
			} else if (c == '\b' || c == 127) {
				return;   // handled as a key press below
			} else if (c >= ' ') {
				last().text.append(c);
			} else {
				return;
			}
			changed();
		});
		canvas.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.BACK_SPACE) {
				backspace();
				e.consume();
			}
		});
		paint();
	}

	/** Called (with no arguments) after every change, so the window can update its status bar. */
	public void setOnChange(Object function) {
		onChange = function instanceof ScriptObjectMirror && ((ScriptObjectMirror) function).isFunction() ? (ScriptObjectMirror) function : null;
	}

	/** Replaces the document: each line becomes a paragraph. Lines starting "# " and "## " become headings. */
	public void setText(String text) {
		paragraphs.clear();
		for (String line : text.replace("\r\n", "\n").split("\n", -1)) {
			Paragraph p = new Paragraph();
			if (line.startsWith("## ")) {
				p.style = "Heading 2";
				line = line.substring(3);
			} else if (line.startsWith("# ")) {
				p.style = "Heading 1";
				line = line.substring(2);
			}
			p.text.append(line);
			paragraphs.add(p);
		}
		changed();
	}

	/** The document as text, headings marked "# " and "## " (what setText reads). */
	public String getText() {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < paragraphs.size(); i++) {
			Paragraph p = paragraphs.get(i);
			if (i > 0) {
				out.append('\n');
			}
			out.append(p.style.equals("Heading 1") ? "# " : p.style.equals("Heading 2") ? "## " : "").append(p.text);
		}
		return out.toString();
	}

	public void clear() {
		setText("");
	}

	/** Types text at the end of the document, as the keyboard does. "\n" starts a new paragraph. */
	public void type(String text) {
		for (char c : text.toCharArray()) {
			if (c == '\n') {
				paragraphs.add(new Paragraph());
			} else {
				last().text.append(c);
			}
		}
		changed();
	}

	public void backspace() {
		Paragraph p = last();
		if (p.text.length() > 0) {
			p.text.setLength(p.text.length() - 1);
		} else if (paragraphs.size() > 1) {
			paragraphs.remove(paragraphs.size() - 1);
		}
		changed();
	}

	/** font-weight (bold/normal), font-style (italic/normal), font-size, font-family: for the whole document. */
	public void applyTextAttribute(String name, String value) {
		switch (name) {
		case "font-weight":
			bold = "bold".equals(value);
			break;
		case "font-style":
			italic = "italic".equals(value);
			break;
		case "font-size":
			fontSize = Double.parseDouble(value.replace("px", ""));
			break;
		case "font-family":
			fontFamily = value;
			break;
		default:
			throw new IllegalArgumentException("Unknown text attribute " + name);
		}
		changed();
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}

	/** Sets the style of the paragraph being typed (the last one): Normal, Heading 1 or Heading 2. */
	public void setParagraphStyle(String style) {
		if (!style.equals("Normal") && !style.equals("Heading 1") && !style.equals("Heading 2")) {
			throw new IllegalArgumentException("Unknown style " + style);
		}
		last().style = style;
		changed();
	}

	public String getParagraphStyle() {
		return last().style;
	}

	/** The headings, for the Navigator. */
	public List<String> getHeadings() {
		List<String> headings = new ArrayList<>();
		for (Paragraph p : paragraphs) {
			if (!p.style.equals("Normal")) {
				headings.add((p.style.equals("Heading 2") ? "    " : "") + p.text);
			}
		}
		return Collections.unmodifiableList(headings);
	}

	public int getPageCount() {
		return pageCount;
	}

	public int getParagraphCount() {
		return paragraphs.size();
	}

	public int getWordCount() {
		int words = 0;
		for (Paragraph p : paragraphs) {
			String t = p.text.toString().trim();
			words += t.isEmpty() ? 0 : t.split("\\s+").length;
		}
		return words;
	}

	private Paragraph last() {
		return paragraphs.get(paragraphs.size() - 1);
	}

	private void changed() {
		paint();
		if (onChange != null) {
			onChange.call(null);
		}
	}

	private Font font(String style) {
		double size = style.equals("Heading 1") ? fontSize * 1.75 : style.equals("Heading 2") ? fontSize * 1.35 : fontSize;
		boolean heavy = bold || !style.equals("Normal");
		return Font.font(fontFamily, heavy ? FontWeight.BOLD : FontWeight.NORMAL, italic ? FontPosture.ITALIC : FontPosture.REGULAR, size);
	}

	/** Lays the paragraphs out into lines and pages, and draws the pages that fit on the canvas. */
	private void paint() {
		Text measure = new Text();
		double width = PAGE_WIDTH - 2 * MARGIN;
		double bottom = PAGE_HEIGHT - MARGIN;
		List<List<Object[]>> pages = new ArrayList<>();   // each line: {font, text, y}
		List<Object[]> page = new ArrayList<>();
		pages.add(page);
		double y = MARGIN;
		for (Paragraph p : paragraphs) {
			Font f = font(p.style);
			measure.setFont(f);
			measure.setText("Ag");
			double lineHeight = measure.getLayoutBounds().getHeight() * 1.15;
			for (String line : wrap(p.text.toString(), f, width, measure)) {
				if (y + lineHeight > bottom && !page.isEmpty()) {
					page = new ArrayList<>();
					pages.add(page);
					y = MARGIN;
				}
				page.add(new Object[] { f, line, y });
				y += lineHeight;
			}
			y += fontSize * 0.5;   // space after each paragraph
		}
		pageCount = pages.size();
		if (canvas == null) {
			return;
		}
		GraphicsContext g = canvas.getGraphicsContext2D();
		double cw = canvas.getCanvas().getWidth();
		double ch = canvas.getCanvas().getHeight();
		g.setFill(Color.web("#808080"));
		g.fillRect(0, 0, cw, ch);
		double scale = Math.min(1, (cw - 2 * GAP) / PAGE_WIDTH);
		if (scale <= 0) {
			return;
		}
		g.save();
		g.translate(Math.max(GAP, (cw - PAGE_WIDTH * scale) / 2), GAP);
		g.scale(scale, scale);
		double top = 0;
		for (List<Object[]> lines : pages) {
			if (top * scale > ch) {
				break;
			}
			g.setFill(Color.rgb(0, 0, 0, 0.3));
			g.fillRect(4, top + 4, PAGE_WIDTH, PAGE_HEIGHT);
			g.setFill(Color.WHITE);
			g.fillRect(0, top, PAGE_WIDTH, PAGE_HEIGHT);
			g.setFill(Color.BLACK);
			g.setTextBaseline(VPos.TOP);
			for (Object[] line : lines) {
				g.setFont((Font) line[0]);
				g.fillText((String) line[1], MARGIN, top + (Double) line[2]);
			}
			top += PAGE_HEIGHT + GAP / scale;
		}
		// The caret, after the last character typed.
		List<Object[]> lastPage = pages.get(pages.size() - 1);
		if (!lastPage.isEmpty()) {
			Object[] line = lastPage.get(lastPage.size() - 1);
			measure.setFont((Font) line[0]);
			measure.setText((String) line[1]);
			double x = MARGIN + measure.getLayoutBounds().getWidth() + 1;
			double caretTop = (pages.size() - 1) * (PAGE_HEIGHT + GAP / scale) + (Double) line[2];
			measure.setText("Ag");
			g.setStroke(Color.BLACK);
			g.strokeLine(x, caretTop, x, caretTop + measure.getLayoutBounds().getHeight());
		}
		g.restore();
	}

	/** Breaks a paragraph into lines that fit the page's text width. */
	private static List<String> wrap(String text, Font font, double width, Text measure) {
		List<String> lines = new ArrayList<>();
		measure.setFont(font);
		StringBuilder line = new StringBuilder();
		for (String word : text.split(" ", -1)) {
			String candidate = line.length() == 0 ? word : line + " " + word;
			measure.setText(candidate);
			if (measure.getLayoutBounds().getWidth() <= width || line.length() == 0) {
				line.setLength(0);
				line.append(candidate);
			} else {
				lines.add(line.toString());
				line.setLength(0);
				line.append(word);
			}
		}
		lines.add(line.toString());
		return lines;
	}
}
