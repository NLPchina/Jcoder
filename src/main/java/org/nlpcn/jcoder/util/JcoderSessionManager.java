package org.nlpcn.jcoder.util;

import org.eclipse.jetty.server.session.AbstractSession;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.server.session.HashedSession;
import org.nlpcn.jcoder.domain.JcoderSession;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.filter.AuthoritiesManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Ansj on 22/12/2017.
 */
public class JcoderSessionManager extends AbstractSessionManager {

	public static final JcoderSessionManager manager = new JcoderSessionManager() ;


	@Override
	protected void addSession(AbstractSession session) {
		StaticValue.space().getTokenCache().put(session.getClusterId(), (Token) session.getAttribute(AuthoritiesManager.TOKEN));
	}

	@Override
	public AbstractSession getSession(String idInCluster) {
		Token token = StaticValue.space().getTokenCache().get(idInCluster);
		return null;
	}

	@Override
	protected void shutdownSessions() throws Exception {

	}

	@Override
	protected AbstractSession newSession(HttpServletRequest request) {
		return null;
	}

	@Override
	protected boolean removeSession(String idInCluster) {
		return false;
	}


}
