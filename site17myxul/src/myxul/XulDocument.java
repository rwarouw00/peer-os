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

	XulDocument(XulElement root) {
		this.root = root;
		index(root);
	}

	private void index(XulElement e) {
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
