package org.nlpcn.jcoder.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.domain.UserGroup;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@IocBean
public class LoginAction {
	
	private static final Logger LOG = LoggerFactory.getLogger(LoginAction.class) ;

	public BasicDao basicDao = StaticValue.systemDao;

	@At("/login")
	@Ok("raw")
	public String login(HttpServletRequest req, @Param("name") String name, @Param("password") String password, @Param("verification_code") String verificationCode) {

		String sessionCode = (String) req.getSession().getAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
		if (!sessionCode.equalsIgnoreCase(verificationCode)) {
			return StaticValue.errMessage("Login fail please validate verification code ");
		}

		Condition con = Cnd.where("name", "=", name);
		User user = basicDao.findByCondition(User.class, con);

		if (user != null && user.getPassword().equals(StaticValue.passwordEncoding(password))) {
			HttpSession session = Mvcs.getHttpSession();
			session.setAttribute("user", name);
			session.setAttribute("userId", user.getId());
			session.setAttribute("userType", user.getType());
			Condition co = null;
			if (user.getType() != 1) {
				List<UserGroup> userGroupList = basicDao.search(UserGroup.class, Cnd.where("userId", "=", user.getId()));
				Long[] ids = new Long[userGroupList.size()];
				Map<Long, Integer> authMap = new HashMap<>();
				for (int i = 0; i < ids.length; i++) {
					ids[i] = userGroupList.get(i).getGroupId();
					authMap.put(ids[i], userGroupList.get(i).getAuth());
				}
				List<Group> GroupList = basicDao.search(Group.class, Cnd.where("id", "in", ids));

				session.setAttribute("AUTH_MAP", authMap);
				session.setAttribute("GROUP_LIST", GroupList);
			} else {
				List<Group> groups = basicDao.search(Group.class, co);
				session.setAttribute("GROUP_LIST", groups);
			}
			LOG.info("user " + name + "login ok");

			return StaticValue.OK;
		} else {
			LOG.info("user " + name + "login err");
			return StaticValue.errMessage("login fail please validate your name or password");
		}
	}

	@At("/register")
	@Ok("redirect:/login.jsp")
	public void register(@Param("name") String name, @Param("password") String password, @Param("authority") Integer authority) {
		Integer userAuth = (Integer) Mvcs.getHttpSession().getAttribute("authority");
		if (userAuth.equals(1)) {
			User user = new User();
			user.setName(name);
			user.setPassword(password);
			user.setCreateTime(new Date());
			try {
				basicDao.save(user);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@At("/loginOut")
	@Ok("redirect:/login.jsp")
	public void loginOut() {
		HttpSession session = Mvcs.getHttpSession();
		session.removeAttribute("user");
		session.removeAttribute("authority");
		session.removeAttribute("uGroups");
		session.removeAttribute("userType");
	}

	@At("/checkName/?")
	@Ok("raw")
	public boolean checkName(@Param("name") String name) {
		Condition con = Cnd.where("name", "=", name);
		User user = basicDao.findByCondition(User.class, con);
		if (user == null) {
			return true;
		} else {
			return false;
		}
	}
}
