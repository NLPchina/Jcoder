package org.nlpcn.jcoder.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.nlpcn.jcoder.domain.Token;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * api token mananger
 * 
 * @author Ansj
 *
 */
public class TokenService {

	private static final Cache<String, Token> TOKEN_CACHE = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).build();

	public static Token getToken(String key) throws ExecutionException {
		Token token = TOKEN_CACHE.get(key, () -> {
			return Token.NULL;
		});
		return token;
	}

	public static void putToken(String key, Token token) {
		TOKEN_CACHE.put(key, token);
	}
}
