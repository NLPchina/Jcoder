package org.nlpcn.jcoder.service;

import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.util.ZKMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.nlpcn.jcoder.util.StaticValue.space;

/**
 * api token mananger
 *
 * @author Ansj
 */
public class TokenService {

	private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);


	/**
	 * 获得一个token
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static Token getToken(String key) throws Exception {
		ZKMap<Token> tokenCache = space().getTokenCache();

		Token token = tokenCache.get(key);

		if (token == null) {
			return null;
		}

		long time = token.getExpirationTime().getTime() - System.currentTimeMillis();

		if (time < 0) {
			tokenCache.remove(key);
			return null;
		}

		if (time < token.getExpiration() / 2) {
			token.setExpirationTime(new Date(System.currentTimeMillis() + token.getExpiration()));
			tokenCache.put(token.getToken(), token);
		}

		return token;
	}

	/**
	 * regeidt a token by user
	 */
	public static String regToken(User user) throws Exception {
		return regToken(user, UUID.randomUUID().toString());
	}

	/**
	 * regeidt a token by user
	 *
	 * @param key ,用户自定义自己的token，用户自己保持不重复
	 */
	public static String regToken(User user, String key) throws Exception {
		LOG.info(user.getName() + " to create a key");
		Token token = new Token();
		token.setToken(key);
		token.setCreateTime(new Date());
		token.setExpirationTime(new Date(System.currentTimeMillis() + token.getExpiration()));
		token.setUser(user);
		space().getTokenCache().put(token.getToken(), token);
		return token.getToken();
	}

	/**
	 * login out by token
	 */
	public static Token removeToken(String key) throws Exception {
		Token token = space().getTokenCache().remove(key);
		LOG.info(token + " to removed ");
		return token;
	}
}
