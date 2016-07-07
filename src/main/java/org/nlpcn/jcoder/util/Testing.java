package org.nlpcn.jcoder.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.lang.Mirror;

/**
 * Test your task
 * 
 * @author Ansj
 *
 */
public class Testing {
	

	/**
	 * instan task by ioc
	 * 
	 * @param c
	 * @return class c instance
	 * @throws Exception
	 */
	public static <T> T instance(Class<T> c) throws Exception {

		Ioc ioc = new NutIoc(new JsonLoader("resource/ioc.js"));

		Mirror<?> mirror = Mirror.me(c);
		T obj = c.newInstance();

		for (Field field : mirror.getFields()) {
			Inject inject = field.getAnnotation(Inject.class);
			if (inject != null) {
				if (field.getType().equals(Logger.class)) {
					mirror.setValue(obj, field, Logger.getLogger(c));
				} else {
					mirror.setValue(obj, field, ioc.get(field.getType(), StringUtil.isBlank(inject.value()) ? field.getName() : inject.value()));
				}
			}
		}
		
		
		return obj;
	}

	/**
	 * instance empty request
	 * 
	 * @return
	 */
	public HttpServletRequest req() {

		return new HttpServletRequest() {

			@Override
			public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
				return null;
			}

			@Override
			public AsyncContext startAsync() throws IllegalStateException {
				return null;
			}

			@Override
			public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

			}

			@Override
			public void setAttribute(String name, Object o) {

			}

			@Override
			public void removeAttribute(String name) {

			}

			@Override
			public boolean isSecure() {
				return false;
			}

			@Override
			public boolean isAsyncSupported() {
				return false;
			}

			@Override
			public boolean isAsyncStarted() {
				return false;
			}

			@Override
			public ServletContext getServletContext() {
				return null;
			}

			@Override
			public int getServerPort() {
				return 0;
			}

			@Override
			public String getServerName() {
				return null;
			}

			@Override
			public String getScheme() {
				return null;
			}

			@Override
			public RequestDispatcher getRequestDispatcher(String path) {
				return null;
			}

			@Override
			public int getRemotePort() {

				return 0;
			}

			@Override
			public String getRemoteHost() {

				return null;
			}

			@Override
			public String getRemoteAddr() {

				return null;
			}

			@Override
			public String getRealPath(String path) {

				return null;
			}

			@Override
			public BufferedReader getReader() throws IOException {

				return null;
			}

			@Override
			public String getProtocol() {

				return null;
			}

			@Override
			public String[] getParameterValues(String name) {

				return null;
			}

			@Override
			public Enumeration<String> getParameterNames() {

				return null;
			}

			@Override
			public Map<String, String[]> getParameterMap() {
				return null;
			}

			@Override
			public String getParameter(String name) {
				return null;
			}

			@Override
			public Enumeration<Locale> getLocales() {

				return null;
			}

			@Override
			public Locale getLocale() {

				return null;
			}

			@Override
			public int getLocalPort() {

				return 0;
			}

			@Override
			public String getLocalName() {

				return null;
			}

			@Override
			public String getLocalAddr() {

				return null;
			}

			@Override
			public ServletInputStream getInputStream() throws IOException {

				return null;
			}

			@Override
			public DispatcherType getDispatcherType() {

				return null;
			}

			@Override
			public String getContentType() {

				return null;
			}

			@Override
			public long getContentLengthLong() {

				return 0;
			}

			@Override
			public int getContentLength() {

				return 0;
			}

			@Override
			public String getCharacterEncoding() {

				return null;
			}

			@Override
			public Enumeration<String> getAttributeNames() {
				return null;
			}

			@Override
			public Object getAttribute(String name) {
				return null;
			}

			@Override
			public AsyncContext getAsyncContext() {
				return null;
			}

			@Override
			public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
				return null;
			}

			@Override
			public void logout() throws ServletException {

			}

			@Override
			public void login(String username, String password) throws ServletException {

			}

			@Override
			public boolean isUserInRole(String role) {

				return false;
			}

			@Override
			public boolean isRequestedSessionIdValid() {

				return false;
			}

			@Override
			public boolean isRequestedSessionIdFromUrl() {

				return false;
			}

			@Override
			public boolean isRequestedSessionIdFromURL() {

				return false;
			}

			@Override
			public boolean isRequestedSessionIdFromCookie() {

				return false;
			}

			@Override
			public Principal getUserPrincipal() {

				return null;
			}

			@Override
			public HttpSession getSession(boolean create) {

				return null;
			}

			@Override
			public HttpSession getSession() {

				return null;
			}

			@Override
			public String getServletPath() {

				return null;
			}

			@Override
			public String getRequestedSessionId() {

				return null;
			}

			@Override
			public StringBuffer getRequestURL() {

				return null;
			}

			@Override
			public String getRequestURI() {

				return null;
			}

			@Override
			public String getRemoteUser() {

				return null;
			}

			@Override
			public String getQueryString() {

				return null;
			}

			@Override
			public String getPathTranslated() {

				return null;
			}

			@Override
			public String getPathInfo() {

				return null;
			}

			@Override
			public Collection<Part> getParts() throws IOException, ServletException {

				return null;
			}

			@Override
			public Part getPart(String name) throws IOException, ServletException {

				return null;
			}

			@Override
			public String getMethod() {

				return null;
			}

			@Override
			public int getIntHeader(String name) {

				return 0;
			}

			@Override
			public Enumeration<String> getHeaders(String name) {

				return null;
			}

			@Override
			public Enumeration<String> getHeaderNames() {

				return null;
			}

			@Override
			public String getHeader(String name) {

				return null;
			}

			@Override
			public long getDateHeader(String name) {

				return 0;
			}

			@Override
			public Cookie[] getCookies() {

				return null;
			}

			@Override
			public String getContextPath() {

				return null;
			}

			@Override
			public String getAuthType() {

				return null;
			}

			@Override
			public String changeSessionId() {

				return null;
			}

			@Override
			public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
				return false;
			}
		};
	}

	/**
	 * instance empty response
	 * 
	 * @return
	 */
	public HttpServletResponse resp() {
		return new HttpServletResponse() {

			@Override
			public void setLocale(Locale loc) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setContentType(String type) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setContentLengthLong(long len) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setContentLength(int len) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setCharacterEncoding(String charset) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setBufferSize(int size) {
				// TODO Auto-generated method stub

			}

			@Override
			public void resetBuffer() {
				// TODO Auto-generated method stub

			}

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isCommitted() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ServletOutputStream getOutputStream() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Locale getLocale() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getContentType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getCharacterEncoding() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getBufferSize() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void flushBuffer() throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setStatus(int sc, String sm) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setStatus(int sc) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setIntHeader(String name, int value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setHeader(String name, String value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setDateHeader(String name, long date) {
				// TODO Auto-generated method stub

			}

			@Override
			public void sendRedirect(String location) throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void sendError(int sc, String msg) throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void sendError(int sc) throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public int getStatus() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Collection<String> getHeaders(String name) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Collection<String> getHeaderNames() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getHeader(String name) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String encodeUrl(String url) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String encodeURL(String url) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String encodeRedirectUrl(String url) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String encodeRedirectURL(String url) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean containsHeader(String name) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void addIntHeader(String name, int value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void addHeader(String name, String value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void addDateHeader(String name, long date) {
				// TODO Auto-generated method stub

			}

			@Override
			public void addCookie(Cookie cookie) {
				// TODO Auto-generated method stub

			}
		};
	}

}
