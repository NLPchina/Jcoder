package org.nlpcn.jcoder.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.domain.UserGroup;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

@IocBean
@Filters(@By(type = AuthoritiesManager.class, args = { "userType", "1", "/login.jsp" }))
@Ok("json")
public class UserAction {

	private static final Logger LOG = LoggerFactory.getLogger(UserAction.class) ;


	public BasicDao basicDao = StaticValue.systemDao;;

	@At("/admin/user/list")
	public Restful userList() {
		
		JSONObject result = new JSONObject() ;
		
		Condition con = null;
		List<User> users = basicDao.search(User.class, con);
		result.put("users", users);

		List<Group> groups = basicDao.search(Group.class, con);
		result.put("groups", groups);
		
		return Restful.instance().obj(result) ;
		
	}

	@At("/admin/group/list")
	public Restful groupList() {
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
		JSONObject result = new JSONObject() ;
		result.put("groups", groups);
		return Restful.instance().obj(result) ;
	}

	@At("/user/nameDiff")
	@Ok("raw")
	public boolean userNameDiff(@Param("name") String name) {
		Condition con = Cnd.where("name", "=", name);
		int count = basicDao.searchCount(User.class, con);
		return count == 0;
	}


	@At("/admin/user/add")
	public Restful addU(@Param("..") User user) throws Exception {
		//User user = JSONObject.parseObject(userInfo).toJavaObject(User.class);
		if (userNameDiff(user.getName())) {
			user.setPassword(StaticValue.passwordEncoding(user.getPassword()));
			user.setCreateTime(new Date());
			basicDao.save(user);
			LOG.info("add user:" + user.getName());
			return Restful.OK.msg("添加成功！");
		}else{
			return Restful.ERR.msg("有相同名字用户！");
		}
		
	}

	@At("/admin/user/del")
	public Restful delU(@Param("..") User user) {
		// TODO 删除用户后要把用户相关的userTask及userGroup删除，等userTask完成后做
		if (user.getType() == 1) {
			// 保证至少要有一个超级用户
			Condition con = Cnd.where("type", "=", 1);
			int count = basicDao.searchCount(User.class, con);
			if (count == 1) {
				LOG.info("fail to del the last super user!");
				return Restful.ERR.msg("至少保证有一个超级管理员！");
			}
		}
		boolean flag = basicDao.delById(user.getId(), User.class);
		if (flag) {
			LOG.info("del user:" + user.getName());
			return Restful.OK.msg("删除用户："+ user.getName()+"成功！");
		}
		Condition co = Cnd.where("userId", "=", user.getId());
		int num = basicDao.delByCondition(UserGroup.class, co);
		if (num > 0) {
			LOG.info("del userGroup's num:" + num);
		}
		return Restful.OK;
	}

	@At("/admin/user/modify")
	public Restful modify(@Param("..") User user) throws Exception {
		if (user == null) {
			return Restful.ERR.msg("修改失败！");
		}
		
		User dbUser = basicDao.find(user.getId(), User.class);

		if (!user.getPassword().equals(dbUser.getPassword())) {
			user.setPassword(StaticValue.passwordEncoding(user.getPassword()));
		}

		boolean flag = basicDao.update(user);
		if (flag) {
			LOG.info("modify user:" + user);
			return Restful.OK.msg("修改成功！");
		}
		return Restful.OK;
	}



}
