package cn.com.infcn.api.test;

import java.util.concurrent.ExecutionException;

import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.service.TokenService;
import org.nlpcn.jcoder.util.Restful;

import com.google.common.collect.ImmutableMap;


public class PmpUserApi {

	private static final String ANONYMOUS_USER = "anonymous";



	/**
	 * 用户登录
	 *
	 * @param name 用户名
	 * @param password 密码
	 * @param verificationCode 验证码
	 * @param cookie
	 * @return
	 * @throws ExecutionException
	 */
	@Execute
	public Restful login(String name, String password, String verificationCode, String cookie) throws Exception {
		return Restful.instance(ImmutableMap.of("token", TokenService.regToken(new User()), "name", ANONYMOUS_USER));
	}

	
}

