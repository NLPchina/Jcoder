package org.nlpcn.jcoder.controller;

import java.io.File;

import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
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
@Filters(@By(type = AuthoritiesManager.class, args = { "userType", "1", "/login.jsp" }))
public class IocAction {

	@At("/ioc")
	@Ok("jsp:/ioc.jsp")
	public String show() {
		return IOUtil.getContent(new File(StaticValue.HOME + "/resource/ioc.js"), IOUtil.UTF8);
	}

	@At("/ioc/save")
	@Ok("json")
	public JsonResult save(@Param("code") String code) {
		IOUtil.Writer(StaticValue.HOME + "/resource/ioc.js", IOUtil.UTF8, code);
		Ioc ioc = new NutIoc(new JsonLoader(StaticValue.HOME + "/resource/ioc.js"));
		if(StaticValue.getUserIoc()!=null){
			StaticValue.getUserIoc().depose();	
		}
		StaticValue.setUserIoc(ioc);
		return StaticValue.okMessageJson("保存并加载成功！");
	}
}
