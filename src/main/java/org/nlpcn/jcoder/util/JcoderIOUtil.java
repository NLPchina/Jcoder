package org.nlpcn.jcoder.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class JcoderIOUtil {

	public static byte[] input2Bytes(InputStream is) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			byte[] bytes = new byte[2048];

			int len = 0;
			while ((len = is.read(bytes)) > 0) {
				bos.write(bytes, 0, len);
			}
			return bos.toByteArray();
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
}
