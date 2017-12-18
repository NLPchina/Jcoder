package org.nlpcn.jcoder.service;

import java.util.List;

import org.nutz.ioc.loader.annotation.IocBean;

@IocBean
public interface IocService {
	public List<String> getAllHosts() throws Exception;
}
