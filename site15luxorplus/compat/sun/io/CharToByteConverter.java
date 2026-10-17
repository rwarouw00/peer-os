package sun.io;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * Stand-in for the JDK 1.x converter that Jacl 1.3.3 uses to write text, built
 * on java.nio.charset, because Java 8 removed sun.io. Only what Jacl calls is
 * here. Characters the encoding can't represent are replaced (that's what the
 * "Any" in convertAny meant).
 */
public class CharToByteConverter {

	private final CharsetEncoder encoder;
	private int nextChar;
	private int nextByte;

	private CharToByteConverter(Charset charset) {
		encoder = charset.newEncoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
	}

	public static CharToByteConverter getConverter(String encoding) throws UnsupportedEncodingException {
		try {
			return new CharToByteConverter(Charset.forName(encoding));
		} catch (IllegalArgumentException e) {
			throw new UnsupportedEncodingException(encoding);
		}
	}

	/** Encodes in[inStart..inEnd) into out[outStart..outEnd); throws ConversionBufferFullException if out fills up. */
	public int convertAny(char[] in, int inStart, int inEnd, byte[] out, int outStart, int outEnd)
			throws ConversionBufferFullException {
		CharBuffer src = CharBuffer.wrap(in, inStart, inEnd - inStart);
		ByteBuffer dst = ByteBuffer.wrap(out, outStart, outEnd - outStart);
		CoderResult result = encoder.encode(src, dst, false);
		nextChar = src.position();
		nextByte = dst.position();
		if (result.isOverflow()) {
			throw new ConversionBufferFullException();
		}
		return nextByte - outStart;
	}

	/** The encoder keeps no bytes back between calls, so there's nothing to flush. */
	public int flushAny(byte[] out, int outStart, int outEnd) {
		return 0;
	}

	public int nextCharIndex() {
		return nextChar;
	}

	public int nextByteIndex() {
		return nextByte;
	}

	public void reset() {
		encoder.reset();
		nextChar = 0;
		nextByte = 0;
	}
}
