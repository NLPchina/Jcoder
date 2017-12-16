package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.filter.IpErrorCountFilter;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 * Created by Ansj on 14/12/2017.
 */

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@Ok("json")
public class MainAction {

	private static final Logger LOG = LoggerFactory.getLogger(TaskAction.class);

	@Inject
	private TaskService taskService;

	@Inject
	private GroupService groupService;

	@At("/admin/main/left")
	public Restful left() throws Exception {

		JSONArray result = new JSONArray() ;

		User user = (User) Mvcs.getHttpSession().getAttribute("user");

		boolean isAdmin = user.getType()  == 1 ;

		List<String> allGroups = groupService.getAllGroupNames();

		//task 管理
		JSONArray submenus = new JSONArray() ;
		for (String groupName : allGroups) {
			submenus.add(ImmutableMap.of("name",groupName,"url","task/list.html?name="+groupName)) ;
		}
		result.add(ImmutableMap.of("name","Task管理","submenus",submenus)) ;


		//jar 管理
		submenus = new JSONArray() ;
		for (String groupName : allGroups) {
			submenus.add(ImmutableMap.of("name",groupName,"url","jar/list.html?name="+groupName)) ;
		}
		result.add(ImmutableMap.of("name","Jar管理","submenus",submenus)) ;


		//Resource管理
		submenus = new JSONArray() ;
		for (String groupName : allGroups) {
			submenus.add(ImmutableMap.of("name",groupName,"url","resource/list.html?name="+groupName)) ;
		}
		result.add(ImmutableMap.of("name","Resource管理","submenus",submenus)) ;



		if(isAdmin){
			submenus = new JSONArray() ;
			submenus.add(ImmutableMap.of("name","用户管理","url","user/list.html")) ;
			submenus.add(ImmutableMap.of("name","Group管理","url","group/list.html")) ;
			result.add(ImmutableMap.of("name","系统管理","submenus",submenus)) ;
		}

		return Restful.instance().obj(result) ;

	}
}