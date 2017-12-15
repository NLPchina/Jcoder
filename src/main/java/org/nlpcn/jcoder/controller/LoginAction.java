package org.nlpcn.jcoder.controller;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.domain.UserGroup;
import org.nlpcn.jcoder.filter.IpErrorCountFilter;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.service.TokenService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

@IocBean
@Filters(@By(type = IpErrorCountFilter.class, args = { "20" }))
@Ok("json")
public class LoginAction {

	private static final Logger LOG = LoggerFactory.getLogger(LoginAction.class);

	public BasicDao basicDao = StaticValue.systemDao;

	@At("/admin/login")
	public Restful login(HttpServletRequest req, HttpServletResponse resp, @Param("name") String name, @Param("password") String password) throws Throwable {
		JSONObject restful = new JSONObject();
		Condition con = Cnd.where("name", "=", name);
		User user = basicDao.findByCondition(User.class, con);

		if (user != null && user.getPassword().equals(StaticValue.passwordEncoding(password))) {
			restful.put("user", name);
			restful.put("userId", user.getId());
			restful.put("userType", user.getType());
			
			HttpSession session = Mvcs.getHttpSession();
			session.setAttribute("user", user);

			LOG.info("user " + name + "login ok");

			if(!StaticValue.IS_LOCAL) { //集群模式相互访问使用token
				session.setAttribute("userToken", TokenService.regToken(user));
			}

			return Restful.OK.obj(restful) ;
		} else {
			int err = IpErrorCountFilter.err();
			LOG.info("user " + name + "login err ,times : " + err);
			return Restful.ERR ;
		}
	}
	

	private static final String origin = "*";
	private static final String methods = "get, post, put, delete, options";
	private static final String headers = "origin, content-type, accept, authorization";
	private static final String credentials = "true";

	@At("/login/api")
	@Ok("json")
	public Restful loginApi(HttpServletRequest req,HttpServletResponse resp, @Param("name") String name, @Param("password") String password) throws Exception {
		resp.addHeader("Access-Control-Allow-Origin", origin);
		resp.addHeader("Access-Control-Allow-Methods", methods);
		resp.addHeader("Access-Control-Allow-Headers", headers);
		resp.addHeader("Access-Control-Allow-Credentials", credentials);
		
		int err = IpErrorCountFilter.err();// for client login to many times , 
		Condition con = Cnd.where("name", "=", name);
		User user = basicDao.findByCondition(User.class, con);
		if (user != null && user.getPassword().equals(StaticValue.passwordEncoding(password))) {
			return Restful.instance().obj(TokenService.regToken(user));
		} else {
			LOG.info("user " + name + "login err , times : " + err);
			return Restful.instance(false, "login fail please validate your name or password ,times : " + err, null, ApiException.Unauthorized);
		}
	}

	@At("/loginOut/api")
	@Ok("json")
	public Restful loginOutApi(HttpServletRequest req) throws Exception {
		String token = req.getHeader("authorization");
		if (StringUtil.isBlank(token)) {
			return Restful.instance(false, "token 'authorization' not in header ");
		} else {
			Token removeToken = TokenService.removeToken(token);
			if (removeToken == null) {
				return Restful.instance(false, "token not in server ");
			} else {
				return Restful.instance(true, removeToken.getUser().getName() + " login out ok");
			}

		}
	}

	@At("/validation/token")
	public void validation(String token) throws Exception {
		Token t = TokenService.getToken("token");
		if (t == null) {
			new JsonView(ApiException.NotFound);
		} else {
			new JsonView();
		}
	}

	@At("/admin/loginOut")
	@Ok("redirect:/login.html")
	public void loginOut() {
		HttpSession session = Mvcs.getHttpSession();
		session.removeAttribute("user");
		session.removeAttribute("userId");
		session.removeAttribute("userType");
		try {
			StaticValue.space().removeToken(String.valueOf(session.getAttribute("userToken"))) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		session.removeAttribute("userToken");
	}

}
