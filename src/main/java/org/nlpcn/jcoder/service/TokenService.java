package org.nlpcn.jcoder.service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.util.SharedSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * api token mananger
 * 
 * @author Ansj
 *
 */
public class TokenService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TokenService.class) ;


	public static Token getToken(String key) throws ExecutionException {
		return SharedSpace.getToken(key) ;
	}
	
	/**
	 * regeidt a token by user
	 * @param user
	 * @return
	 * @throws ExecutionException 
	 */
	public static String regToken(User user) throws ExecutionException{
		LOG.info(user.getName()+" to create a key");
		String key = UUID.randomUUID().toString();
		Token token = new Token();
		token.setToken(key);
		token.setCreateTime(new Date());
		token.setUser(user);
		SharedSpace.regToken(token) ;
		return token.getToken();
	}
	
	/**
	 * login out by token
	 * @param key
	 * @return
	 */
	public static Token removeToken(String key){
		Token token = SharedSpace.removeToken(key) ;
		LOG.info(token+" to create a key");
		return token ;
	}
}
