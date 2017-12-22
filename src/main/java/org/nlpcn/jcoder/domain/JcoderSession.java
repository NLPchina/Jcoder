package org.nlpcn.jcoder.domain;

import org.eclipse.jetty.server.session.AbstractSession;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.nlpcn.jcoder.util.StaticValue;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

/**
 * 简化的session管理。不要使用这段代码到大规模用户中
 * Created by Ansj on 22/12/2017.
 */
public class JcoderSession extends AbstractSession implements Serializable {

	private static final long serialVersionUID = 5208464051134226143L;

	private Token token;

	protected JcoderSession(AbstractSessionManager abstractSessionManager, HttpServletRequest request) {
		super(abstractSessionManager, request);
	}

	protected JcoderSession(AbstractSessionManager abstractSessionManager, long created, long accessed, String clusterId) {
		super(abstractSessionManager, created, accessed, clusterId);
	}


	@Override
	public Map<String, Object> getAttributeMap() {
		return token.getParams();
	}

	@Override
	public int getAttributes() {
		return token.getParams().size();
	}

	@Override
	public Set<String> getNames() {
		synchronized (token.getParams()) {
			return new HashSet<String>(token.getParams().keySet());
		}
	}

	@Override
	public void clearAttributes() {
		synchronized (token.getParams()) {
			token.getParams().clear();
			StaticValue.space().getTokenCache().put(token.getToken(), token);
		}
	}

	@Override
	public Object doPutOrRemove(String name, Object value) {

		return value == null ? token.getParams().remove(name) : token.getParams().put(name, value);
	}

	@Override
	public Object doGet(String name) {
		return token.getParams().get(name);
	}

	@Override
	public Enumeration<String> doGetAttributeNames() {
		Hashtable<String, Object> ht = new Hashtable<>(token.getParams());
		return ht.keys();
	}
}
