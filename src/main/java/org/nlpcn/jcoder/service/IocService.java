package org.nlpcn.jcoder.service;

import java.util.Set;

import org.nutz.ioc.loader.annotation.IocBean;

@IocBean
public interface IocService {
	public Set<String> getAllHosts() throws Exception;
}
