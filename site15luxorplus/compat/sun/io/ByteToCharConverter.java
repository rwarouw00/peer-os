package sun.io;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * Stand-in for the JDK 1.x converter that Jacl 1.3.3 uses to read text, built
 * on java.nio.charset, because Java 8 removed sun.io. Only what Jacl calls is
 * here. Bytes that end in the middle of a character are left unconsumed
 * (nextByteIndex stops before them), and Jacl passes them in again with the
 * next read. Bad bytes become U+FFFD instead of an error.
 */
public class ByteToCharConverter {

	private final CharsetDecoder decoder;
	private int nextByte;
	private int nextChar;

	private ByteToCharConverter(Charset charset) {
		decoder = charset.newDecoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
	}

	public static ByteToCharConverter getConverter(String encoding) throws UnsupportedEncodingException {
		try {
			return new ByteToCharConverter(Charset.forName(encoding));
		} catch (IllegalArgumentException e) {
			throw new UnsupportedEncodingException(encoding);
		}
	}

	/** Decodes in[inStart..inEnd) into out[outStart..outEnd); throws ConversionBufferFullException if out fills up. */
	public int convert(byte[] in, int inStart, int inEnd, char[] out, int outStart, int outEnd)
			throws MalformedInputException, UnknownCharacterException, ConversionBufferFullException {
		ByteBuffer src = ByteBuffer.wrap(in, inStart, inEnd - inStart);
		CharBuffer dst = CharBuffer.wrap(out, outStart, outEnd - outStart);
		CoderResult result = decoder.decode(src, dst, false);
		nextByte = src.position();
		nextChar = dst.position();
		if (result.isOverflow()) {
			throw new ConversionBufferFullException();
		}
		return nextChar - outStart;
	}

	public int nextByteIndex() {
		return nextByte;
	}

	public int nextCharIndex() {
		return nextChar;
	}

	public void reset() {
		decoder.reset();
		nextByte = 0;
		nextChar = 0;
	}
}
