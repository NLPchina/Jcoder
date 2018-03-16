package org.nlpcn.jcoder.util.dao;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.AuthenticationProvider;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.apache.zookeeper.server.auth.IPAuthenticationProvider;
import org.nlpcn.jcoder.util.StaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * 单薄的zk客户端
 * Created by Ansj on 05/12/2017.
 */
public class ZookeeperDao implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperDao.class);

	private CuratorFramework client = null;

	public ZookeeperDao(String connStr) throws NoSuchAlgorithmException {

		String[] split = connStr.split("\\|");

		connStr = split[0];
		String authorStr = null;

		AuthenticationProvider authProvider = null;

		if (split.length > 2) {
			LOG.error("connStr err: {} more than one split `|` so use world author", connStr);
		}

		if (split.length == 2) {
			authorStr = split[1];
			authProvider = new DigestAuthenticationProvider();
		} else if (StaticValue.IS_LOCAL) {
			authorStr = "127.0.0.1";
			authProvider = new IPAuthenticationProvider();
		}

		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString(connStr).retryPolicy(new RetryNTimes(10, 2000));

		List<ACL> defaultACL = new ArrayList<>();
		if (StaticValue.IS_LOCAL) { //如果单机模式只有本机可以访问
			defaultACL.add(new ACL(ZooDefs.Perms.ALL, new Id(authProvider.getScheme(), authorStr)));
		} else {
			defaultACL.add(new ACL(ZooDefs.Perms.ALL, new Id(authProvider.getScheme(), DigestAuthenticationProvider.generateDigest(authorStr))));
		}

		try {
			builder.authorization(authProvider.getScheme(), authorStr.getBytes("utf-8")).aclProvider(new ACLProvider() {
				@Override
				public List<ACL> getDefaultAcl() {
					return defaultACL;
				}

				@Override
				public List<ACL> getAclForPath(String path) {
					return defaultACL;
				}
			});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		client = builder.build();
	}

	public ZookeeperDao start() {
		client.start();
		return this;
	}

	@Override
	public void close() {
		client.close();
	}

	public CuratorFramework getZk() {
		return client;
	}

}
