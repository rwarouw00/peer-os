package sun.io;

/** Stand-in for the JDK 1.x class of the same name, which Java 8 removed. */
public class ConversionBufferFullException extends java.io.CharConversionException {
	public ConversionBufferFullException() {
	}

	public ConversionBufferFullException(String message) {
		super(message);
	}
}
