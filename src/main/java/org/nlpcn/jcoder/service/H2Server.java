package org.nlpcn.jcoder.service;

import java.io.File;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.h2.tools.Server;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.mvc.NutConfig;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * h2数据库
 * 
 * @author ansj
 * 
 */
public class H2Server {
	private static final Logger LOG = Logger.getLogger(H2Server.class);

	private static Server server;

	public static void startServer(NutConfig nc) {

		if (server != null && server.isRunning(true)) {
			return;
		}
		try {
			LOG.info("database path is " + "正在启动h2...");

			String h2db = StaticValue.HOME + "/h2db/jcoder";

			boolean dbIsActive = new File(h2db+".h2.db").isFile();

			DruidDataSource dds = new DruidDataSource();
			dds.setDriverClassName("org.h2.Driver");
			dds.setUrl("jdbc:h2:" + h2db);
			dds.setUsername("sa");
			dds.setPassword("");
			dds.setInitialSize(10);
			dds.setMaxActive(100);
			dds.setTestOnReturn(true);
			dds.setValidationQuery("select 1");
			BasicDao basicDao = new BasicDao(dds);

			StaticValue.systemDao = basicDao;

			LOG.info("database path is " + new File(h2db).getAbsolutePath());
			server = Server.createPgServer(new String[] { "-baseDir", h2db }).start();
			

			if (!dbIsActive) {
				LOG.warn("the database is not create , use db script to create it!");
				String content = IOUtil.getContent(H2Server.class.getResourceAsStream("/jcoder.sql"), IOUtil.UTF8);
				basicDao.executeSql(content);
			}else{
				LOG.info("the database is active good luck for use it !");
			}
		} catch (SQLException e) {
			LOG.error("启动h2出错：" + e.toString());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		LOG.info("数据库启动完毕...");
	}

	public static void stopServer() {
		if (server != null) {
			System.out.println("正在关闭h2...");
			server.stop();
			System.out.println("关闭成功.");
		}
	}

}
