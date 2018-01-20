package org.nlpcn.jcoder.job;

import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.nlpcn.jcoder.server.H2Server;
import org.nlpcn.jcoder.server.ZKServer;
import org.nlpcn.jcoder.server.rpc.websocket.WebSocketServer;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import java.util.Arrays;

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
		WebSocketServer.stopServer();
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


		// 启动rpc服务,默认是当前端口+1 ;
		LOG.info("begin start rpc server! on port " + StaticValue.RPCPORT);
		try {
			WebSocketServer.startServer(StaticValue.RPCPORT);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			LOG.error(StaticValue.PREFIX + "port not set in system property");
		} catch (Exception e) {
			LOG.error("rpc server stop fail ", e);
		}

//		WebAppContext.Context ct = (WebAppContext.Context) nc.getServletContext();
//		WebAppContext webAppContext = (WebAppContext) ct.getContextHandler();
//
//		try {
//			ServerContainer configureContext = WebSocketServerContainerInitializer.configureContext(webAppContext);
//			Arrays.stream(nc.getIoc().getNames()).forEach(name -> {
//				Object object = nc.getIoc().get(Object.class, name);
//				if (object.getClass().getAnnotation(ServerEndpoint.class) != null) {
//					try {
//						configureContext.addEndpoint(object.getClass());
//						LOG.info("add " + object.getClass() + " in websocket container");
//					} catch (Exception e) {
//						LOG.error("add " + object.getClass() + " in websocket container fail!!", e);
//					}
//				}
//			});
//		} catch (ServletException e) {
//			e.printStackTrace();
//		}

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
