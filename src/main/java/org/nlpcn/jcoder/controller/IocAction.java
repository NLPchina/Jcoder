package org.nlpcn.jcoder.controller;

import java.io.File;

import org.nlpcn.jcoder.util.IOUtil;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.util.JsonResult;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
public class IocAction {

	@At("/ioc")
	@Ok("jsp:/ioc.jsp")
	public String show(@Param("groupName") String groupName) {
		JarService jarService = JarService.getOrCreate(groupName) ;
		return IOUtil.getContent(new File(jarService.getIocPath()), IOUtil.UTF8);
	}

	@At("/ioc/save")
	@Ok("json")
	public JsonResult save(@Param("groupName") String groupName, @Param("code") String code) {
		try {
			JarService jarService = JarService.getOrCreate(groupName) ;
			IOUtil.Writer(jarService.getIocPath(), IOUtil.UTF8, code);
			jarService.release();
			return StaticValue.okMessageJson("保存并加载成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return StaticValue.okMessageJson("保存失败！" + e.getMessage());
		}
	}
}
