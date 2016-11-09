package org.nlpcn.jcoder.server.rpc.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.BlockingArrayQueue;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 虚拟文件用以流的传输
 * 
 * @author ansj
 *
 */
public class VFile {

	public static final ConcurrentHashMap<String, Object> BUFFERED_MAP = new ConcurrentHashMap<String, Object>();

	public static final ConcurrentHashMap<String, InputStream> STREAM_MAP = new ConcurrentHashMap<String, InputStream>();

	public static final byte[] END_BYTE = new byte[] { 'j', 'C', 'o', 'D', 'e', 'R', 'v', 'F', 'e', 'n', 'd' };

	public static final byte[] ERR_BYTE = new byte[] { 'j', 'C', 'o', 'D', 'e', 'R', 'v', 'F', 'e', 'r', 'r' };

	public static final String VFILE_CLIENT = "__JCODER_VFile_CLIENT_METHOD";

	public static final String VFILE_SERVER = "__JCODER_VFile_SERVER_METHOD";
	
	public static final String VFILE_LOCAL = "__JCODER_VFile_LOCAL_METHOD";

	private static final int LEN = 1024 * 64;

	private String id;

	private String name;

	private String clientLocalPath;

	private boolean file;

	private boolean directory;

	private long lastModified;

	private long length;

	private boolean hidden;

	private boolean canRead;

	private VFile[] listVFiles;

	private String verification;

	private long off;

	private int len = LEN;

	private BlockingQueue<byte[]> queue;

	private long beginTime;

	public VFile() {
	}

	public VFile(InputStream input) {
		this.id = UUID.randomUUID().toString();
		this.name = input.toString();
		this.clientLocalPath = name;
		this.file = false;
		this.directory = false;
		this.lastModified = System.currentTimeMillis();
		this.length = 0;
		this.canRead = true;
		this.hidden = false;
		this.verification = Util.encrypt(id + clientLocalPath);
		STREAM_MAP.put(id, input);
	}
	
	public VFile(File file) {
		file = new File(file.getAbsolutePath());// 构造绝对路径的file
		this.id = UUID.randomUUID().toString();
		this.name = file.getName();
		this.clientLocalPath = file.getAbsolutePath();
		this.file = file.isFile();
		this.directory = file.isDirectory();
		this.lastModified = file.lastModified();
		this.length = file.length();
		this.canRead = file.canRead();
		this.hidden = file.isHidden();
		this.verification = Util.encrypt(id + clientLocalPath);

		if (canRead && directory) {
			File[] listFiles = file.listFiles();
			listVFiles = new VFile[listFiles.length];
			for (int i = 0; i < listFiles.length; i++) {
				listVFiles[i] = new VFile(listFiles[i]);
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getClientLocalPath() {
		return clientLocalPath;
	}

	public boolean isFile() {
		return file;
	}

	public boolean isDirectory() {
		return directory;
	}

	public long getLastModified() {
		return lastModified;
	}

	public long getLength() {
		return length;
	}

	public VFile[] getListVFiles() {
		return listVFiles;
	}

	public String getId() {
		return id;
	}

	public boolean isHidden() {
		return hidden;
	}

	public boolean isCanRead() {
		return canRead;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setClientLocalPath(String clientLocalPath) {
		this.clientLocalPath = clientLocalPath;
	}

	public void setFile(boolean file) {
		this.file = file;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

	public long getOff() {
		return off;
	}

	public void setOff(long off) {
		this.off = off;
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
	}

	public void setListVFiles(VFile[] listVFiles) {
		this.listVFiles = listVFiles;
	}

	public byte[] readBytes() throws IOException, InterruptedException {
		return readBytes(LEN);
	}

	public long getBeginTime() {
		return beginTime;
	}

	/**
	 * return all bytes by file if file is too big it throw OutOfMemory
	 * 
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public byte[] readAllByte() throws IOException, InterruptedException {
		byte[] result = null;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			byte[] bytes = null;

			while ((bytes = readBytes()) != null) {
				bos.write(bytes);
			}

			result = bos.toByteArray();
		}
		return result;
	}

	/**
	 * write this vfile to local file
	 * 
	 * @param path local path
	 */
	public void writeToFile(File root, String fileName) throws IOException, InterruptedException {
		String path = root.getAbsolutePath();
		if (this.isDirectory()) {
			File parent = makeDirectory(path, fileName);
			if (this.listVFiles != null) {
				for (VFile f : listVFiles) {
					f.writeToFile(parent, null);
				}
			}
		} else {
			_writeToFile(path, fileName);
		}
	}

	private File makeDirectory(String path, String fileName) {
		File outFile = new File(path + File.separator + (fileName == null ? this.name : fileName));
		if (!outFile.exists()) {
			outFile.mkdirs();
		}
		return outFile;
	}

	private void _writeToFile(String path, String fileName) throws IOException, InterruptedException {
		File outFile = new File(path + File.separator + (fileName == null ? this.name : fileName));
		try (FileOutputStream fos = new FileOutputStream(outFile)) {
			byte[] bytes = null;
			while ((bytes = readBytes()) != null) {
				fos.write(bytes);
			}
		}
	}

	public boolean check() {
		return Util.check(id + clientLocalPath, verification);
	}

	/**
	 * read byte by len
	 * 
	 * @param len
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private byte[] readBytes(int len) throws InterruptedException, IOException {
		if (Rpcs.getReq() != null) { // 说明当前在服务器端,需要从客户端读取
			return readClient(len);
		} else {
			return readServer(len);
		}
	}

	/**
	 * 转换为输入流
	 * @return
	 */
	public RpcInputStream toInputStream() {
		return new RpcInputStream(this);
	}

	/**
	 * 从服务器端读取流到客户端
	 * @param len
	 * @return
	 * @throws IOException 
	 */
	private byte[] readServer(int len) throws IOException {
		RpcRequest req = new RpcRequest(UUID.randomUUID().toString(), VFILE_SERVER, VFILE_SERVER, true, false, 120000L, new Object[] { this });
		byte[] bytes = null;
		try {
			Object result = RpcClient.getInstance().proxy(req);
			if (result instanceof String) {
				throw new IOException((String) result);
			} else {
				bytes = (byte[]) result;
				if (bytes != null) {
					off += bytes.length;
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		return bytes;
	}

	/**
	 * 从客户端读取流到服务器端
	 * @param len
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private byte[] readClient(int len) throws InterruptedException, IOException {
		beginTime = System.currentTimeMillis();

		if (queue == null) {
			queue = new BlockingArrayQueue<>();
		}

		this.len = len;

		Channel channel = Rpcs.getContext().getChContext().channel();

		try {
			RpcResponse rep = new RpcResponse(this.getId());
			rep.setFile(true);
			rep.setResult(this);
			BUFFERED_MAP.put(this.getId(), this);

			channel.writeAndFlush(rep).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);

			byte[] poll = queue.poll(20, TimeUnit.MINUTES);
			if (poll == END_BYTE) {
				return null;
			}

			if (poll == ERR_BYTE) {
				throw new IOException("client have some err to throws you will see the err in log!");
			}

			this.off += poll.length;

			return poll;

		} finally {
			BUFFERED_MAP.remove(this.getId());
		}
	}

	public void close() throws IOException {
		InputStream inputStream = STREAM_MAP.get(this.id);
		if (inputStream != null) {
			inputStream.close();
		}

		if (BUFFERED_MAP.contains(this.id)) {
			BUFFERED_MAP.remove(this.id);
		}
	}

	public void addBytes(byte[] bytes) {
		queue.add(bytes);
	}

	public void resetStream() {
		this.off = 0;
		this.len = LEN;
	}

}
