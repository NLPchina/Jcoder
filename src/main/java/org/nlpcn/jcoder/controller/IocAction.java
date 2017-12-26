package org.nlpcn.jcoder.controller;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.util.*;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.IocService;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/ioc")
@Ok("json")
@Fail("http:500")
public class IocAction {
	
	private static final Logger LOG = LoggerFactory.getLogger(GroupAction.class);

	@Inject
	private IocService iocService;

	@Inject
	private ProxyService proxyService;

	private BasicDao basicDao = StaticValue.systemDao;

	@At
	public Restful save(@Param("hostPorts") String[] hostPorts,@Param("groupName") String groupName, @Param("code") String code,
						@Param(value = "first", df = "true") boolean first) {
		try {
			if(!first){
				JarService jarService = JarService.getOrCreate(groupName) ;
				jarService.saveIoc(groupName, code);
				//jarService.release();
				return Restful.instance().ok(true).msg("保存成功！");

			}else{
				Set<String> hostPortsArr = new HashSet<>();

				Arrays.stream(hostPorts).forEach(s -> hostPortsArr.add((String) s));

				String message = proxyService.post(hostPortsArr, "/admin/ioc/save", ImmutableMap.of("groupName", groupName,"code", code,"first", false), 100000, ProxyService.MERGE_MESSAGE_CALLBACK);
				iocService.saveIocInfo(groupName,code);
				return Restful.instance().ok(true).msg(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.instance().ok(false).msg("保存失败！" + e.getMessage());
		}
	}

	@At
	public Restful hostList(@Param("groupName") String groupName) {
		try {
			return Restful.ok().obj(iocService.getAllHosts(groupName));
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.fail().msg(e.getMessage());
		}
	}

	@At
	public Restful findIocInfoByGroupName(@Param("groupName") String groupName) {
		try {
			return Restful.ok().obj(iocService.getIocInfo(groupName));
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.fail().msg(e.getMessage());
		}
	}
}
