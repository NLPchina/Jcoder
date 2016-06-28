package org.nlpcn.jcoder.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
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

import com.alibaba.fastjson.JSONObject;

@IocBean
public class LoginAction {

	private Logger log = Logger.getLogger(this.getClass());

	public BasicDao basicDao = StaticValue.systemDao;

	@At("/login")
	@Ok("raw")
	public String login(@Param("name") String name, @Param("password") String password) {
		JSONObject job = new JSONObject();
		Condition con = Cnd.where("name", "=", name);
		User user = basicDao.findByCondition(User.class, con);

		if (user != null && user.getPassword().equals(password)) {
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
			log.info("用户" + name + "登录成功！");
			job.put("ok", true);
			return job.toJSONString();
		} else {
			log.info("用户" + name + "登录失败！");
			job.put("ok", false);
			return job.toJSONString();
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
