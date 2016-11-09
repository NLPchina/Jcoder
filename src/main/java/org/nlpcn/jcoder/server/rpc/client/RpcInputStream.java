package org.nlpcn.jcoder.server.rpc.client;

import java.io.IOException;
import java.io.InputStream;

public class RpcInputStream extends InputStream {

	private VFile file;

	private byte[] bytes;

	private int off = -1;

	public RpcInputStream(VFile file) {
		this.file = file;
	}

	@Override
	public int read() throws IOException {
		if (bytes == null || off + 1 == bytes.length) {
			try {
				bytes = file.readBytes();
				off = -1;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			if (bytes == null || bytes.length == 0) {
				return -1;
			}
		}
		return bytes[++off];
	}

}
