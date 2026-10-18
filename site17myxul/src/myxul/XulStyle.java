package myxul;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Translates a XUL element's inline style (web CSS) into JavaFX: most
 * properties become JavaFX CSS (-fx-...), sizes become preferred sizes,
 * margin becomes a box margin and box-shadow a DropShadow effect. JavaFX CSS
 * looks like web CSS but uses its own property names, so web CSS can't be
 * passed through as it is.
 */
final class XulStyle {

	private static final Pattern LENGTH = Pattern.compile("(-?[0-9.]+)(px|pt|em)?");

	/** What was done with one style: the JavaFX CSS, and the declarations that were ignored. */
	static final class Result {
		final StringBuilder fx = new StringBuilder();
		final List<String> ignored = new ArrayList<>();
	}

	/** Applies the style to the node, and returns the declarations it couldn't use. */
	static List<String> apply(XulElement element, Node node, String style) {
		Result result = new Result();
		for (String declaration : style.split(";")) {
			int colon = declaration.indexOf(':');
			if (colon < 0) {
				if (!declaration.trim().isEmpty()) {
					result.ignored.add(declaration.trim());
				}
				continue;
			}
			String name = declaration.substring(0, colon).trim().toLowerCase();
			String value = declaration.substring(colon + 1).trim();
			if (!translate(element, node, name, value, result)) {
				result.ignored.add(name + ": " + value);
			}
		}
		if (result.fx.length() > 0) {
			String existing = node.getStyle();
			node.setStyle((existing == null || existing.isEmpty() ? "" : existing + " ") + result.fx);
		}
		return result.ignored;
	}

	private static boolean translate(XulElement element, Node node, String name, String value, Result out) {
		boolean region = node instanceof Region;
		switch (name) {
		case "color":
			if (node instanceof Labeled || node instanceof TextInputControl) {
				out.fx.append("-fx-text-fill: ").append(value).append(";");
				return true;
			}
			return false;
		case "background":
		case "background-color":
			if (!region) {
				return false;
			}
			out.fx.append("-fx-background-color: ").append(value).append(";");
			return true;
		case "background-image":
			if (!region) {
				return false;
			}
			Matcher u = Pattern.compile("url\\(\\s*[\"']?([^\"')]*)[\"']?\\s*\\)").matcher(value);
			if (!u.find()) {
				return value.trim().equals("none") && out.fx.append("-fx-background-image: null;").length() > 0;
			}
			out.fx.append("-fx-background-image: url(\"").append(XulRegistry.url(element, u.group(1))).append("\");");
			return true;
		case "background-size":
		case "background-position":
		case "background-repeat":
			if (!region) {
				return false;
			}
			out.fx.append("-fx-").append(name).append(": ").append(value).append(";");
			return true;
		case "font-family":
			out.fx.append("-fx-font-family: ").append(fontFamily(value)).append(";");
			return true;
		case "font-size":
			out.fx.append("-fx-font-size: ").append(length(value)).append(";");
			return true;
		case "font-weight":
		case "font-style":
			out.fx.append("-fx-").append(name).append(": ").append(value).append(";");
			return true;
		case "padding":
			if (!region) {
				return false;
			}
			out.fx.append("-fx-padding: ").append(lengths(value)).append(";");
			return true;
		case "border":
		case "border-top":
		case "border-right":
		case "border-bottom":
		case "border-left":
			return region && border(name, value, out);
		case "border-radius":
			if (!region) {
				return false;
			}
			out.fx.append("-fx-border-radius: ").append(lengths(value)).append("; -fx-background-radius: ").append(lengths(value)).append(";");
			return true;
		case "opacity":
			out.fx.append("-fx-opacity: ").append(value).append(";");
			return true;
		case "margin":
			XulBox.setMargin(node, insets(value));
			return true;
		case "width":
		case "height":
		case "min-width":
		case "min-height":
		case "max-width":
		case "max-height":
			return size(node, name, value);
		case "box-shadow":
			return shadow(node, value);
		default:
			return false;
		}
	}

	/** "monospace" is web CSS's name; JavaFX calls it "monospaced". */
	private static String fontFamily(String value) {
		StringBuilder fx = new StringBuilder();
		for (String family : value.split(",")) {
			String f = family.trim();
			String bare = f.replaceAll("^[\"']|[\"']$", "");
			switch (bare.toLowerCase()) {
			case "monospace":
				f = "monospaced";
				break;
			case "sans-serif":
			case "serif":
			case "monospaced":
			case "system":
				f = bare.toLowerCase();
				break;
			default:
				f = "\"" + bare + "\"";
				break;
			}
			if (fx.length() > 0) {
				fx.append(", ");
			}
			fx.append(f);
		}
		return fx.toString();
	}

	/** "14px" stays 14px; "10pt" stays 10pt; a bare number gets px. */
	private static String length(String value) {
		Matcher m = LENGTH.matcher(value.trim());
		if (m.matches()) {
			return m.group(1) + (m.group(2) == null ? "px" : m.group(2));
		}
		return value;
	}

	private static String lengths(String value) {
		StringBuilder fx = new StringBuilder();
		for (String part : value.trim().split("\\s+")) {
			fx.append(fx.length() == 0 ? "" : " ").append(length(part));
		}
		return fx.toString();
	}

	private static double px(String value) {
		Matcher m = LENGTH.matcher(value.trim());
		if (!m.matches()) {
			throw new NumberFormatException(value);
		}
		double n = Double.parseDouble(m.group(1));
		return "pt".equals(m.group(2)) ? n * 4 / 3 : "em".equals(m.group(2)) ? n * 13 : n;
	}

	/** CSS's 1 to 4 values: all; vertical horizontal; top horizontal bottom; top right bottom left. */
	private static Insets insets(String value) {
		String[] p = value.trim().split("\\s+");
		double[] v = new double[p.length];
		for (int i = 0; i < p.length; i++) {
			v[i] = px(p[i]);
		}
		switch (v.length) {
		case 1:
			return new Insets(v[0]);
		case 2:
			return new Insets(v[0], v[1], v[0], v[1]);
		case 3:
			return new Insets(v[0], v[1], v[2], v[1]);
		default:
			return new Insets(v[0], v[1], v[2], v[3]);
		}
	}

	/** "1px solid #ccc", on all sides or on one. */
	private static boolean border(String name, String value, Result out) {
		String width = "1px";
		String style = "solid";
		String color = "black";
		for (String part : value.trim().split("\\s+(?![^(]*\\))")) {
			if (LENGTH.matcher(part).matches()) {
				width = length(part);
			} else if (part.matches("solid|dashed|dotted|none|hidden")) {
				style = part.equals("hidden") ? "none" : part;
			} else {
				color = part;
			}
		}
		if (style.equals("none")) {
			width = "0";
		}
		String widths = width;
		switch (name) {
		case "border-top":
			widths = width + " 0 0 0";
			break;
		case "border-right":
			widths = "0 " + width + " 0 0";
			break;
		case "border-bottom":
			widths = "0 0 " + width + " 0";
			break;
		case "border-left":
			widths = "0 0 0 " + width;
			break;
		default:
			break;
		}
		out.fx.append("-fx-border-color: ").append(color).append("; -fx-border-width: ").append(widths)
				.append("; -fx-border-style: ").append(style.equals("none") ? "solid" : style).append(";");
		return true;
	}

	private static boolean size(Node node, String name, String value) {
		if (!(node instanceof Region)) {
			return false;
		}
		Region r = (Region) node;
		double v = px(value);
		switch (name) {
		case "width":
			r.setPrefWidth(v);
			break;
		case "height":
			r.setPrefHeight(v);
			break;
		case "min-width":
			r.setMinWidth(v);
			break;
		case "min-height":
			r.setMinHeight(v);
			break;
		case "max-width":
			r.setMaxWidth(v);
			XulBox.setLimited(r);
			break;
		default:
			r.setMaxHeight(v);
			XulBox.setLimited(r);
			break;
		}
		return true;
	}

	/** "2px 2px 5px rgba(0,0,0,0.3)": x, y, blur and colour. */
	private static boolean shadow(Node node, String value) {
		String colorText = "rgba(0,0,0,0.5)";
		Matcher fn = Pattern.compile("(rgba?\\([^)]*\\)|#[0-9a-fA-F]{3,8}|[a-zA-Z]+)\\s*$").matcher(value.trim());
		String rest = value.trim();
		if (fn.find()) {
			colorText = fn.group(1);
			rest = rest.substring(0, fn.start()).trim();
		}
		String[] parts = rest.isEmpty() ? new String[0] : rest.split("\\s+");
		if (parts.length < 2) {
			return false;
		}
		DropShadow shadow = new DropShadow();
		shadow.setOffsetX(px(parts[0]));
		shadow.setOffsetY(px(parts[1]));
		shadow.setRadius(parts.length > 2 ? Math.max(1, px(parts[2])) : 1);
		shadow.setColor(Color.web(colorText));
		node.setEffect(shadow);
		return true;
	}

	private XulStyle() {
	}
}
