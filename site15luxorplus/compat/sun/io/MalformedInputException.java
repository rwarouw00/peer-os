package sun.io;

/** Stand-in for the JDK 1.x class of the same name, which Java 8 removed. */
public class MalformedInputException extends java.io.CharConversionException {
	public MalformedInputException() {
	}

	public MalformedInputException(String message) {
		super(message);
	}
}
