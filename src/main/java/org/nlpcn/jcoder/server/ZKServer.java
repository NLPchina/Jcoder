package org.nlpcn.jcoder.server;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.h2.tools.Server;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.mvc.NutConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

public class ZKServer {
	private static final Logger LOG = LoggerFactory.getLogger(H2Server.class);

	public static void startServer(NutConfig nc) {
		Properties props = new Properties();
		props.setProperty("tickTime", "2000");
		props.setProperty("dataDir", new File(System.getProperty("java.io.tmpdir"), "zookeeper").getAbsolutePath());
		props.setProperty("clientPort", String.valueOf(StaticValue.PORT+2));
		props.setProperty("initLimit", "10");
		props.setProperty("syncLimit", "5");

		QuorumPeerConfig quorumConfig = new QuorumPeerConfig();
		try {
			quorumConfig.parseProperties(props);
			final ZooKeeperServerMain zkServer = new ZooKeeperServerMain();
			final ServerConfig config = new ServerConfig();
			config.readFrom(quorumConfig);
			zkServer.runFromConfig(config);
			LOG.info("start zk server ok");
		} catch (Exception e) {
			LOG.error("Start standalone server faile", e);
		}

	}

	public static void stopServer() {
		//:TODO not support
	}
}
