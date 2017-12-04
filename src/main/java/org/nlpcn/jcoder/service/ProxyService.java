package org.nlpcn.jcoder.service;


import com.google.common.collect.Sets;
import org.nutz.http.*;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@IocBean
public class ProxyService {

	private static final Logger LOG = LoggerFactory.getLogger(ProxyService.class);

	private static final String PROXY_HEADER = "PROXY_HEADER" ;

	protected static final Set<String> HOP_HEADERS = Sets.newHashSet("Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
			"TE", "Trailers", "Transfer-Encoding", "Upgrade","Content-Encoding");

	/**
	 * 执行请求
	 *
	 * @param req
	 * @param req
	 * @throws IOException
	 * @return true 代表经过代理，false代表不需要代理
	 */
	public boolean service(HttpServletRequest req, HttpServletResponse rep, String targetUrl)
			throws ServletException, IOException {

		if(req.getHeader(PROXY_HEADER)!=null){
			LOG.warn("SKIP "+targetUrl+" because it header has "+PROXY_HEADER);//这个错误不会发生
			return false;
		}

		StringBuilder uri = new StringBuilder() ;

		uri.append(targetUrl) ;

		String pathInfo = req.getServletPath();
		if (pathInfo != null) {//ex: /my/path.html
			uri.append(encodeUriQuery(pathInfo, true));
		}

		String queryString = req.getQueryString();//ex:(following '?'): name=value&foo=bar#fragment

		if (queryString != null && queryString.length() > 0) {
			uri.append('?');
			uri.append(encodeUriQuery(queryString, false));
		}

		Request request = Request.create(uri.toString(), Request.METHOD.valueOf(req.getMethod()), new HashMap<>(req.getParameterMap()) ,makeHeader(req));

		if(req.getInputStream()!=null){
			request.setInputStream(req.getInputStream()) ;
		}
		
		Response response = Sender.create(request, -1).send();



		Header header = response.getHeader();
		rep.setStatus(response.getStatus());
		Set<Map.Entry<String, String>> all = header.getAll();

		for (Map.Entry<String, String> e: all) {
			if(e.getKey()!=null && !HOP_HEADERS.contains(e.getKey())) {
				rep.setHeader(e.getKey(), e.getValue());
			}
		}
		Streams.write(rep.getOutputStream(), response.getStream());
		return true ;
	}

	/**
	 * 构建请求头
	 * @param req
	 * @return
	 */
	private Header makeHeader(HttpServletRequest req) {
		Enumeration<String> headerNames = req.getHeaderNames();
		Header header = Header.create() ;
		while(headerNames.hasMoreElements()){
			String key = headerNames.nextElement() ;
			header.set(key,req.getHeader(key)) ;
		}
		header.set(PROXY_HEADER,"true") ;
		return header ;
	}

	/**
	 * Encodes characters in the query or fragment part of the URI.
	 *
	 * <p>Unfortunately, an incoming URI sometimes has characters disallowed by the spec.  HttpClient
	 * insists that the outgoing proxied request has a valid URI because it uses Java's {@link URI}.
	 * To be more forgiving, we must escape the problematic characters.  See the URI class for the
	 * spec.
	 *
	 * @param in example: name=value&amp;foo=bar#fragment
	 * @param encodePercent determine whether percent characters need to be encoded
	 */
	protected static CharSequence encodeUriQuery(CharSequence in, boolean encodePercent) {
		//Note that I can't simply use URI.java to encode because it will escape pre-existing escaped things.
		StringBuilder outBuf = null;
		Formatter formatter = null;
		for(int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			boolean escape = true;
			if (c < 128) {
				if (asciiQueryChars.get((int)c) && !(encodePercent && c == '%')) {
					escape = false;
				}
			} else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {//not-ascii
				escape = false;
			}
			if (!escape) {
				if (outBuf != null)
					outBuf.append(c);
			} else {
				//escape
				if (outBuf == null) {
					outBuf = new StringBuilder(in.length() + 5*3);
					outBuf.append(in,0,i);
					formatter = new Formatter(outBuf);
				}
				//leading %, 0 padded, width 2, capital hex
				formatter.format("%%%02X",(int)c);//TODO
			}
		}
		return outBuf != null ? outBuf : in;
	}

	protected static final BitSet asciiQueryChars;
	static {
		char[] c_unreserved = "_-!.~'()*".toCharArray();//plus alphanum
		char[] c_punct = ",;:$&+=".toCharArray();
		char[] c_reserved = "?/[]@".toCharArray();//plus punct

		asciiQueryChars = new BitSet(128);
		for(char c = 'a'; c <= 'z'; c++) asciiQueryChars.set((int)c);
		for(char c = 'A'; c <= 'Z'; c++) asciiQueryChars.set((int)c);
		for(char c = '0'; c <= '9'; c++) asciiQueryChars.set((int)c);
		for(char c : c_unreserved) asciiQueryChars.set((int)c);
		for(char c : c_punct) asciiQueryChars.set((int)c);
		for(char c : c_reserved) asciiQueryChars.set((int)c);

		asciiQueryChars.set((int)'%');//leave existing percent escapes in place
	}


}
