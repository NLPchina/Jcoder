package org.nlpcn.jcoder.service;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class ProxyService {

	private static final String METHOD_DELETE = "DELETE";
	private static final String METHOD_HEAD = "HEAD";
	private static final String METHOD_GET = "GET";
	private static final String METHOD_OPTIONS = "OPTIONS";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_PUT = "PUT";
	private static final String METHOD_TRACE = "TRACE";

	private static final String AllOW_MEHTODS = "DELETE, HEAD, GET, OPTIONS, POST, PUT, TRACE";


	private static final String HEADER_IFMODSINCE = "If-Modified-Since";
	private static final String HEADER_LASTMOD = "Last-Modified";

	private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";

	private static ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);


	/**
	 * 代理http請求
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String method = req.getMethod();

		if (method.equals(METHOD_GET)) {
			long lastModified = getLastModified(req);
			if (lastModified == -1) {
				// servlet doesn't support if-modified-since, no reason
				// to go through further expensive logic
				doGet(req, resp);
			} else {
				long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
				if (ifModifiedSince < lastModified) {
					// If the servlet mod time is later, call doGet()
					// Round down to the nearest second for a proper compare
					// A ifModifiedSince of -1 will always be less
					maybeSetLastModified(resp, lastModified);
					doGet(req, resp);
				} else {
					resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				}
			}

		} else if (method.equals(METHOD_HEAD)) {
			long lastModified = getLastModified(req);
			maybeSetLastModified(resp, lastModified);
			doHead(req, resp);

		} else if (method.equals(METHOD_POST)) {
			doPost(req, resp);

		} else if (method.equals(METHOD_PUT)) {
			doPut(req, resp);

		} else if (method.equals(METHOD_DELETE)) {
			doDelete(req, resp);

		} else if (method.equals(METHOD_OPTIONS)) {
			doOptions(req, resp);

		} else if (method.equals(METHOD_TRACE)) {
			doTrace(req, resp);

		} else {
			//
			// Note that this means NO servlet supports whatever
			// method was requested, anywhere on this server.
			//

			String errMsg = lStrings.getString("http.method_not_implemented");
			Object[] errArgs = new Object[1];
			errArgs[0] = method;
			errMsg = MessageFormat.format(errMsg, errArgs);

			resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String protocol = req.getProtocol();
		String msg = lStrings.getString("http.method_get_not_supported");
		if (protocol.endsWith("1.1")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}


	protected long getLastModified(HttpServletRequest req) {
		return -1;
	}


	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		NoBodyResponse response = new NoBodyResponse(resp);

		doGet(req, response);
		response.setContentLength();
	}


	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String protocol = req.getProtocol();
		String msg = lStrings.getString("http.method_post_not_supported");
		if (protocol.endsWith("1.1")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}


	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String protocol = req.getProtocol();
		String msg = lStrings.getString("http.method_put_not_supported");
		if (protocol.endsWith("1.1")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}


	protected void doDelete(HttpServletRequest req,
	                        HttpServletResponse resp)
			throws ServletException, IOException {
		String protocol = req.getProtocol();
		String msg = lStrings.getString("http.method_delete_not_supported");
		if (protocol.endsWith("1.1")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}


	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setHeader("Allow", AllOW_MEHTODS);
	}


	protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int responseLength;

		String CRLF = "\r\n";
		StringBuilder buffer = new StringBuilder("TRACE ").append(req.getRequestURI())
				.append(" ").append(req.getProtocol());

		Enumeration<String> reqHeaderEnum = req.getHeaderNames();

		while (reqHeaderEnum.hasMoreElements()) {
			String headerName = reqHeaderEnum.nextElement();
			buffer.append(CRLF).append(headerName).append(": ")
					.append(req.getHeader(headerName));
		}

		buffer.append(CRLF);

		responseLength = buffer.length();

		resp.setContentType("message/http");
		resp.setContentLength(responseLength);
		ServletOutputStream out = resp.getOutputStream();
		out.print(buffer.toString());
	}


	/*
	 * Sets the Last-Modified entity header field, if it has not
	 * already been set and if the value is meaningful.  Called before
	 * doGet, to ensure that headers are set before response data is
	 * written.  A subclass might have set this header already, so we
	 * check.
	 */
	private void maybeSetLastModified(HttpServletResponse resp,
	                                  long lastModified) {
		if (resp.containsHeader(HEADER_LASTMOD))
			return;
		if (lastModified >= 0)
			resp.setDateHeader(HEADER_LASTMOD, lastModified);
	}
}


/*
 * A response that includes no body, for use in (dumb) "HEAD" support.
 * This just swallows that body, counting the bytes in order to set
 * the content length appropriately.  All other methods delegate directly
 * to the wrapped HTTP Servlet Response object.
 */
// file private
class NoBodyResponse extends HttpServletResponseWrapper {

	private static final ResourceBundle lStrings
			= ResourceBundle.getBundle("javax.servlet.http.LocalStrings");

	private NoBodyOutputStream noBody;
	private PrintWriter writer;
	private boolean didSetContentLength;
	private boolean usingOutputStream;

	// file private
	NoBodyResponse(HttpServletResponse r) {
		super(r);
		noBody = new NoBodyOutputStream();
	}

	// file private
	void setContentLength() {
		if (!didSetContentLength) {
			if (writer != null) {
				writer.flush();
			}
			setContentLength(noBody.getContentLength());
		}
	}

	@Override
	public void setContentLength(int len) {
		super.setContentLength(len);
		didSetContentLength = true;
	}

	@Override
	public void setContentLengthLong(long len) {
		super.setContentLengthLong(len);
		didSetContentLength = true;
	}

	@Override
	public void setHeader(String name, String value) {
		super.setHeader(name, value);
		checkHeader(name);
	}

	@Override
	public void addHeader(String name, String value) {
		super.addHeader(name, value);
		checkHeader(name);
	}

	@Override
	public void setIntHeader(String name, int value) {
		super.setIntHeader(name, value);
		checkHeader(name);
	}

	@Override
	public void addIntHeader(String name, int value) {
		super.addIntHeader(name, value);
		checkHeader(name);
	}

	private void checkHeader(String name) {
		if ("content-length".equalsIgnoreCase(name)) {
			didSetContentLength = true;
		}
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {

		if (writer != null) {
			throw new IllegalStateException(
					lStrings.getString("err.ise.getOutputStream"));
		}
		usingOutputStream = true;

		return noBody;
	}

	@Override
	public PrintWriter getWriter() throws UnsupportedEncodingException {

		if (usingOutputStream) {
			throw new IllegalStateException(
					lStrings.getString("err.ise.getWriter"));
		}

		if (writer == null) {
			OutputStreamWriter w = new OutputStreamWriter(
					noBody, getCharacterEncoding());
			writer = new PrintWriter(w);
		}

		return writer;
	}
}


/*
 * Servlet output stream that gobbles up all its data.
 */
// file private
class NoBodyOutputStream extends ServletOutputStream {

	private static final String LSTRING_FILE =
			"javax.servlet.http.LocalStrings";
	private static ResourceBundle lStrings =
			ResourceBundle.getBundle(LSTRING_FILE);

	private int contentLength = 0;

	// file private
	NoBodyOutputStream() {
	}

	// file private
	int getContentLength() {
		return contentLength;
	}

	@Override
	public void write(int b) {
		contentLength++;
	}

	@Override
	public void write(byte buf[], int offset, int len)
			throws IOException {
		if (buf == null) {
			throw new NullPointerException(
					lStrings.getString("err.io.nullArray"));
		}

		if (offset < 0 || len < 0 || offset + len > buf.length) {
			String msg = lStrings.getString("err.io.indexOutOfBounds");
			Object[] msgArgs = new Object[3];
			msgArgs[0] = Integer.valueOf(offset);
			msgArgs[1] = Integer.valueOf(len);
			msgArgs[2] = Integer.valueOf(buf.length);
			msg = MessageFormat.format(msg, msgArgs);
			throw new IndexOutOfBoundsException(msg);
		}

		contentLength += len;
	}


	public boolean isReady() {
		return false;
	}

	public void setWriteListener(WriteListener writeListener) {

	}

}
