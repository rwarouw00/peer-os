package sun.io;

/** Stand-in for the JDK 1.x class of the same name, which Java 8 removed. */
public class UnknownCharacterException extends java.io.CharConversionException {
	public UnknownCharacterException() {
	}

	public UnknownCharacterException(String message) {
		super(message);
	}
}
