package myxul;

import java.nio.file.Path;

/** A mistake in a XUL file or its scripts, with the file and line it was found on. */
public class XulException extends RuntimeException {

	private final Path file;
	private final int line;

	public XulException(Path file, int line, String message) {
		super(message);
		this.file = file;
		this.line = line;
	}

	public XulException(Path file, int line, String message, Throwable cause) {
		super(message, cause);
		this.file = file;
		this.line = line;
	}

	public Path getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}

	/** "leafpad.xul:23: Unknown tag <foo>" */
	@Override
	public String toString() {
		String where = file == null ? "" : file.getFileName() + (line > 0 ? ":" + line : "") + ": ";
		return where + getMessage();
	}
}
