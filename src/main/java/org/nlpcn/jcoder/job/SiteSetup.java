package org.nlpcn.jcoder.job;

import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.nlpcn.jcoder.run.rpc.websocket.ApiWebsocket;
import org.nlpcn.jcoder.run.rpc.websocket.LogWebsocket;
import org.nlpcn.jcoder.server.H2Server;
import org.nlpcn.jcoder.server.ZKServer;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.GroupFileListener;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;

public class SiteSetup implements Setup {

	private static final Logger LOG = LoggerFactory.getLogger(SiteSetup.class);

	@Override
	public void destroy(NutConfig nc) {
		try {
			StaticValue.space().release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		;
		H2Server.stopServer();
	}

	@Override
	public void init(NutConfig nc) {

		LOG.info("init begin ! ");
		// 设置ioc
		StaticValue.setSystemIoc(nc.getIoc());

		checkServer();

		H2Server.startServer(nc);

		// set version
		nc.getServletContext().setAttribute("VERSION", StaticValue.VERSION);

		/**
		 * 初始化集群记录
		 */
		if (StaticValue.IS_LOCAL) {
			LOG.info("stared server by local model");
			ZKServer.startServer();
			StaticValue.setMaster(true);
		} else {
			LOG.info("stared server by cluster model");
			StaticValue.setMaster(false);
		}

		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}

		try {
			StaticValue.setSharedSpace(new SharedSpaceService().init());
		} catch (Exception e) {
			LOG.error("zookpeer err ", e);
			System.exit(-1);
		}

		//加載所有的group
		GroupService.allLocalGroup().forEach(g -> { //并监听
			JarService.getOrCreate(g.getName());
			if (StaticValue.TESTRING) {
				GroupFileListener.unRegediter(g.getName());
				GroupFileListener.regediter(g.getName());
			}
		});


		new Thread(new CheckDiffJob()).start();

		//开启日志监控
		new Thread(new LogJob()).start();

        // 开启API访问日志统计
        new Thread(new StatisticalJob()).start();

		//init webscoket
		WebAppContext.Context ct = (WebAppContext.Context) nc.getServletContext();
		WebAppContext webAppContext = (WebAppContext) ct.getContextHandler();
		try {
			ServerContainer configureContext = configureContext = WebSocketServerContainerInitializer.configureContext(webAppContext);
			configureContext.addEndpoint((Class<?>) ApiWebsocket.class);
			configureContext.addEndpoint((Class<?>) LogWebsocket.class);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (DeploymentException e) {
			e.printStackTrace();
		}


		LOG.info("start all ok , goodluck YouYou");

	}

	/**
	 * 检查机器是否可以运行
	 */
	private void checkServer() {
//		if (!StaticValue.IS_LOCAL) {
//			if ("127.0.0.1".equals(StaticValue.getHost())
//					|| "localhost".equals(StaticValue.getHost())) {
//				LOG.error("cluster model must set host by LAN IP or domain");
//				System.exit(-1);
//			}
//		}
	}

}
