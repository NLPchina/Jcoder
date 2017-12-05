package org.nlpcn.jcoder.service.impl;

import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.dao.ZookeeperDao;

public class ClusterSharedSpaceSerivce implements SharedSpaceService {

	private ZookeeperDao zookeeperDao ;

	@Override
	public void add2TaskQueue(String name) {

	}

	@Override
	public void counter(Long id, boolean success) {

	}

	@Override
	public long getSuccess(Long id) {
		return 0;
	}

	@Override
	public long getErr(Long id) {
		return 0;
	}

	@Override
	public Token getToken(String key) {
		return null;
	}

	@Override
	public void regToken(Token token) {

	}

	@Override
	public Token removeToken(String key) {
		return null;
	}

	@Override
	public Long poll() throws InterruptedException {
		return null;
	}

	public void setZookeeperDao(ZookeeperDao zookeeperDao){
		this.zookeeperDao = zookeeperDao ;
	}
}
