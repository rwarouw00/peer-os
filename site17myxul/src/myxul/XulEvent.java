package myxul;

/**
 * The event object for XUL events that JavaFX reports as a property change
 * rather than an Event: oninput, onchange, onselect, onfocus, onblur, onload,
 * onclose. Scripts read event.type and event.target as with a JavaFX event.
 */
public final class XulEvent {

	private final String type;
	private final Object target;

	XulEvent(String type, Object target) {
		this.type = type;
		this.target = target;
	}

	/** "input", "change", "select", "focus", "blur", "load", "close" */
	public String getType() {
		return type;
	}

	/** The JavaFX object the event happened on. */
	public Object getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return "XulEvent[" + type + "]";
	}
}
