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

import java.io.Closeable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 单薄的zk客户端
 * Created by Ansj on 05/12/2017.
 */
public class ZookeeperDao implements Closeable {

	private CuratorFramework client = null;

    public ZookeeperDao(String connStr) throws NoSuchAlgorithmException {
        AuthenticationProvider authProvider = new DigestAuthenticationProvider();
        // TODO: 用户名和密码
        String idPassword = "admin:admin";
        List<ACL> defaultACL = new ArrayList<>(
                Collections.singletonList(new ACL(ZooDefs.Perms.ALL, new Id(authProvider.getScheme(), DigestAuthenticationProvider.generateDigest(idPassword)))));
        client = CuratorFrameworkFactory.builder()
                .connectString(connStr)
                .retryPolicy(new RetryNTimes(10, 2000))
                .authorization(authProvider.getScheme(), idPassword.getBytes())
                .aclProvider(new ACLProvider() {
                    @Override
                    public List<ACL> getDefaultAcl() {
                        return defaultACL;
                    }

                    @Override
                    public List<ACL> getAclForPath(String path) {
                        return defaultACL;
                    }
                })
                .build();
    }

	public ZookeeperDao start(){
		client.start();
		return this ;
	}

	@Override
	public void close() {
		client.close();
	}

	public CuratorFramework getZk() {
		return client;
	}
}
