package org.nlpcn.jcoder.util;

import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * java 一个简单的io操作
 *
 * @author ansj
 */
public class IOUtil {

	private static final Logger LOG = LoggerFactory.getLogger(IOUtil.class);

	public static final String UTF8 = "utf-8";
	public static final String GBK = "gbk";
	public static final String TAB = "\t";
	public static final String LINE = "\n";

	public static void Writer(String path, String charEncoding, String content) {
		OutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(path));
			fos.write(content.getBytes(charEncoding));
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(fos);
		}
	}

	/**
	 * 将输入流转化为字节流
	 *
	 * @param inputStream
	 * @param charEncoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static BufferedReader getReader(InputStream inputStream, String charEncoding) throws UnsupportedEncodingException {
		return new BufferedReader(new InputStreamReader(inputStream, charEncoding));
	}

	/**
	 * 读取文件获得正文
	 *
	 * @param path
	 * @param charEncoding
	 * @return
	 */
	public static String getContent(String path, String charEncoding) {
		return getContent(new File(path), charEncoding);
	}

	/**
	 * 从流中读取正文内容
	 *
	 * @param is
	 * @param charEncoding
	 * @return
	 */
	public static String getContent(InputStream is, String charEncoding) {
		BufferedReader reader = null;
		try {
			reader = IOUtil.getReader(is, charEncoding);
			return getContent(reader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	/**
	 * 从文件中读取正文内容
	 *
	 * @param file
	 * @param charEncoding
	 * @return
	 */
	public static String getContent(File file, String charEncoding) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			return getContent(is, charEncoding);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			close(is);
		}
		return "";
	}

	/**
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static String getContent(BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		try {
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				sb.append(temp);
				sb.append("\n");
			}
		} finally {
			close(reader);
		}
		return sb.toString();
	}

	/**
	 * 关闭字符流
	 *
	 * @param reader
	 */
	public static void close(Reader reader) {
		try {
			if (reader != null)
				reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 关闭字节流
	 *
	 * @param is
	 */
	public static void close(InputStream is) {
		try {
			if (is != null)
				is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭字节流
	 *
	 * @param os
	 */
	public static void close(OutputStream os) {
		if (os != null) {
			try {
				os.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	/**
	 * 写入一个流。并且关闭输入输出
	 *
	 * @param is
	 * @param file
	 */
	public static void writeAndClose(InputStream is, File file) throws IOException {
		writeAndClose(is, new FileOutputStream(file));
	}

	/**
	 * 写入一个流。并且关闭输入输出
	 *
	 * @param is
	 * @param os
	 */
	public static void writeAndClose(InputStream is, OutputStream os) throws IOException {
		try {
			byte[] bytes = new byte[10240];
			int len = 0;
			while ((len = is.read(bytes)) != -1) {
				os.write(bytes, 0, len);
			}
		} finally {
			close(os);
			close(is);
		}
	}

	public static void downFile(String url, File file) throws Exception {

		LOG.info("to down {}", url);

		Response response = Http.get(url);

		if (response.getStatus() != 200) {
			throw new Exception("down " + url + " err code: " + response.getStatus());
		}

		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
		try (InputStream is = response.getStream(); FileOutputStream os = new FileOutputStream(file)) {
			byte[] bytes = new byte[10240];
			int len;
			long start = System.currentTimeMillis();
			long end = start;
			int sum = 0;
			while ((len = is.read(bytes)) != -1) {
				os.write(bytes, 0, len);
				end = System.currentTimeMillis();
				sum += len;
				if (end-start>1000) {
					LOG.info("down {} speed {}k/s", url, df.format((sum / (double) ((end - start + 1)))));
					start = end;
					sum = 0;
				}
			}

			LOG.info("down {} speed {}k/s", url, df.format((sum / (double) ((end - start + 1)))));

			LOG.info("down {} ok write to {} ok", url, file.getCanonicalFile());

		}
	}


	/**
	 * 将结果和输出流对接
	 *
	 * @param response
	 * @param response
	 */
	public static void writeAndClose(Response post, HttpServletResponse response) throws IOException {
		Header header = post.getHeader();
		header.keys().forEach(k -> response.addHeader(k, header.get(k)));
		response.setStatus(post.getStatus());
		IOUtil.writeAndClose(post.getStream(), response.getOutputStream());
	}


}