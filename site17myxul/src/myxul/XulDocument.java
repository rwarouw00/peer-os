package myxul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A window's element tree as scripts reach it: bootstrap.js builds the
 * script-side "document" on these methods (getElementById and so on).
 */
public final class XulDocument {

	private final XulElement root;
	private final Map<String, XulElement> ids = new HashMap<>();
	XulWindow window;
	XulElement popupNode;   // the element a context menu was opened on

	XulDocument(XulElement root) {
		this.root = root;
		index(root);
	}

	void index(XulElement e) {
		String id = e.attr("id");
		if (id != null) {
			XulElement other = ids.get(id);
			if (other != null) {
				throw e.error("id=\"" + id + "\" is used twice: also by " + other.where() + ".");
			}
			ids.put(id, e);
		}
		for (XulElement child : e.children) {
			index(child);
		}
	}

	/** Forgets the ids of an element taken out of the document, and of its children. */
	void unindex(XulElement e) {
		String id = e.attr("id");
		if (id != null && ids.get(id) == e) {
			ids.remove(id);
		}
		for (XulElement child : e.children) {
			unindex(child);
		}
	}

	/** An element's id changed (setAttribute("id", ...)) while it is in the document. */
	void renamed(XulElement e, String oldId) {
		if (oldId != null && ids.get(oldId) == e) {
			ids.remove(oldId);
		}
		String id = e.attr("id");
		if (id != null) {
			XulElement other = ids.get(id);
			if (other != null && other != e) {
				throw new IllegalArgumentException("id=\"" + id + "\" is already used by " + other.where() + ".");
			}
			ids.put(id, e);
		}
	}

	/** document.createElement("button"): a new element, shown once it's appended to one in the window. */
	public XulElement createElement(String tag) {
		if (!tag.matches("[a-z][a-z0-9-]*")) {
			throw new IllegalArgumentException("\"" + tag + "\" isn't a XUL tag name.");
		}
		XulElement e = new XulElement(tag, window.file, 0);
		e.namespace = XulParser.XUL_NS;
		e.document = this;
		return e;
	}

	/** The element whose context menu (context="...") is open, or was last. */
	public XulElement getPopupNode() {
		return popupNode;
	}

	public XulElement getElementById(String id) {
		return ids.get(id);
	}

	public List<XulElement> getElementsByTagName(String tag) {
		List<XulElement> found = new ArrayList<>();
		if (tag.equals("*") || root.tag.equals(tag)) {
			found.add(root);
		}
		root.collect(tag, found);
		return found;
	}

	public XulElement getDocumentElement() {
		return root;
	}

	// For bootstrap.js: recognising elements, and keeping each element's wrapper.

	public boolean isElement(Object value) {
		return value instanceof XulElement;
	}

	public boolean isElementList(Object value) {
		return value instanceof List && !((List<?>) value).isEmpty() && ((List<?>) value).get(0) instanceof XulElement;
	}

	public Object wrapperOf(XulElement element) {
		return element.scriptObject;
	}

	public void remember(XulElement element, Object wrapper) {
		element.scriptObject = wrapper;
	}
}
