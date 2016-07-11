package org.nlpcn.jcoder.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

@IocBean
public class PluginAction {

	@At("/plugin/list")
	@Ok("json")
	public File[] list() {
		File[] files = StaticValue.PLUGIN_FILE.listFiles();

		return files;
	}

	public void start() {

	}

}
