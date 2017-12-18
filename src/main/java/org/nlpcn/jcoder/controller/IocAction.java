package org.nlpcn.jcoder.controller;

import java.io.File;

import org.nlpcn.jcoder.util.IOUtil;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.IocService;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.util.JsonResult;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
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
	public JsonResult save(@Param("groupName") String groupName, @Param("code") String code) {
		try {
			JarService jarService = JarService.getOrCreate(groupName) ;
			jarService.saveIoc(groupName, code);
			jarService.release();
			return StaticValue.okMessageJson("保存并加载成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return StaticValue.okMessageJson("保存失败！" + e.getMessage());
		}
	}
	
	@At
	public Restful hostList() throws Exception {
		return Restful.OK.obj(iocService.getAllHosts());
	}
}
