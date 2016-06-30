package org.nlpcn.jcoder.controller;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.domain.UserGroup;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.*;

import java.util.*;

@IocBean
@Filters(@By(type = AuthoritiesManager.class, args = { "userType", "1", "/login.jsp" }))
public class UserAction {

	private Logger log = Logger.getLogger(this.getClass());

	public BasicDao basicDao = StaticValue.systemDao;;

	@At("/user/list")
	@Ok("jsp:/user/user_list.jsp")
	public void userList() {
		Condition con = null;
		List<User> users = basicDao.search(User.class, con);
		Mvcs.getReq().setAttribute("users", users);

		List<Group> groups = basicDao.search(Group.class, con);
		Mvcs.getReq().setAttribute("groups", groups);

	}

	@At("/group/list")
	@Ok("jsp:/user/group_list.jsp")
	public void groupList() {
		Condition con = null;
		List<Group> groups = basicDao.search(Group.class, con);
		// 查找group下所有用户

		for (Group group : groups) {
			List<UserGroup> userGroupList = basicDao.search(UserGroup.class, Cnd.where("groupId", "=", group.getId()));

			List<Map<String, Object>> users = new ArrayList<>();
			for (UserGroup userGroup : userGroupList) {
				Map<String, Object> userInfo = new HashMap<>();
				User user = basicDao.find(userGroup.getUserId(), User.class);
				if (user == null) {
					continue;
				}
				userInfo.put("id", user.getId());
				userInfo.put("createTime", userGroup.getCreateTime());
				userInfo.put("auth", userGroup.getAuth());
				userInfo.put("name", user.getName());
				users.add(userInfo);
			}

			group.setUsers(users);
		}
		Mvcs.getReq().setAttribute("groups", groups);
	}

	@At("/user/nameDiff")
	@Ok("raw")
	public boolean userNameDiff(@Param("name") String name) {
		Condition con = Cnd.where("name", "=", name);
		int count = basicDao.searchCount(User.class, con);
		return count == 0;
	}

	@At("/group/nameDiff")
	@Ok("raw")
	public boolean groupNameDiff(@Param("name") String name) {
		Condition con = Cnd.where("name", "=", name);
		int count = basicDao.searchCount(Group.class, con);
		return count == 0;
	}

	@At("/user/add")
	@Ok("redirect:/user/list")
	@Fail("jsp:/fail.jsp")
	public void addU(@Param("..") User user) throws Exception {
		if (userNameDiff(user.getName())) {
			user.setCreateTime(new Date());
			basicDao.save(user);
			log.info("add user:" + user.getName());
		}
	}

	@At("/user/del")
	@Ok("redirect:/user/list")
	public void delU(@Param("..") User user) {
		// TODO 删除用户后要把用户相关的userTask及userGroup删除，等userTask完成后做
		if (user.getType() == 1) {
			// 保证至少要有一个超级用户
			Condition con = Cnd.where("type", "=", 1);
			int count = basicDao.searchCount(User.class, con);
			if (count == 1) {
				log.info("fail to del the last super user!");
				return;
			}
		}
		boolean flag = basicDao.delById(user.getId(), User.class);
		if (flag) {
			log.info("del user:" + user.getName());
		}
		Condition co = Cnd.where("userId", "=", user.getId());
		int num = basicDao.delByCondition(UserGroup.class, co);
		if (num > 0) {
			log.info("del userGroup's num:" + num);
		}
	}

	@At("/user/modify")
	@Ok("redirect:/user/list")
	@Fail("jsp:/fail.jsp")
	public void modify(@Param("..") User user) throws Exception {
		if (user == null) {
			return;
		}
		boolean flag = basicDao.update(user);
		if (flag) {
			log.info("modify user:" + user);
		}
	}

	@At("/group/add")
	@Ok("redirect:/group/list")
	@Fail("jsp:/fail.jsp")
	public void addG(@Param("..") Group group) throws Exception {
		if (groupNameDiff(group.getName())) {
			group.setCreateTime(new Date());
			basicDao.save(group);
			log.info("add group:" + group.getName());
			List<Group> groups = (List<Group>) Mvcs.getHttpSession().getAttribute("GROUP_LIST");
			groups.add(group);
			Mvcs.getHttpSession().setAttribute("GROUP_LIST", groups);
		}
	}

	@At("/group/del")
	@Ok("redirect:/group/list")
	public void delG(@Param("..") Group group) {
		basicDao.delById(group.getId(), Group.class);
		log.info("del group:" + group.getName());
		Condition con = Cnd.where("groupId", "=", group.getId());
		int num = basicDao.delByCondition(UserGroup.class, con);
		List<Group> groups = (List<Group>) Mvcs.getHttpSession().getAttribute("GROUP_LIST");
		Iterator<Group> it = groups.iterator();
		while (it.hasNext()) {
			Group g = it.next();
			if (g.getId() == group.getId()) {
				it.remove();
			}
		}
		Mvcs.getHttpSession().setAttribute("GROUP_LIST", groups);
		log.info("del userGroup's num:" + num);
	}

	@At("/group/modify")
	@Ok("redirect:/group/list")
	@Fail("jsp:/fail.jsp")
	public void modifyG(@Param("..") Group group) throws Exception {
		if (group == null) {
			return;
		}
		boolean flag = basicDao.update(group);
		if (flag) {
			log.info("modify group:" + group.toString());
		}
	}
}
