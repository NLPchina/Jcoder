package org.nlpcn.jcoder.job;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.scheduler.TaskException;
import org.nlpcn.jcoder.server.H2Server;
import org.nlpcn.jcoder.server.rpc.server.RpcServer;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.IocException;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;

public class SiteSetup implements Setup {

	private static final Logger LOG = Logger.getLogger(SiteSetup.class);

	@Override
	public void destroy(NutConfig nc) {
		H2Server.stopServer();
		RpcServer.stopServer();
	}

	@Override
	public void init(NutConfig nc) {

		LOG.info("init begin ! ");
		// 设置ioc

		StaticValue.setSystemIoc(nc.getIoc());

		H2Server.startServer(nc);

		// set version
		nc.getServletContext().setAttribute("VERSION", StaticValue.VERSION);

		// 初始化Jar环境
		LOG.info("begin Jar init!");
		JarService.init();

		try {
			LOG.info("begin init all task by db !");
			nc.getIoc().get(TaskService.class, "taskService").initTaskFromDB();
			LOG.info("begin init all task ok !");
		} catch (IocException | TaskException e) {
			e.printStackTrace();
			LOG.error(e);
			System.exit(-1);
		}

		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
			LOG.error(e);
		}

		// task 其他定时任务的运行状况
		new Thread(new CheckTaskJob()).start();

		LOG.info("begin run task quene!");
		// 运行队列
		new Thread(new RunTaskJob()).start();

		LOG.info("begin print console job!");
		// 运行日志打印到websocket任务
		new Thread(new PrintConsoleJob()).start();

		// 启动rpc服务,默认是当前端口+1 ;
		LOG.info("begin start rpc server! on port " + StaticValue.RPCPORT);
		try {
			RpcServer.startServer(StaticValue.RPCPORT);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			LOG.error(StaticValue.PREFIX + "port not set in system property");
		} catch (Exception e) {
			LOG.error("rpc server stop fail ", e);
		}

		LOG.info("start all ok , goodluck");

	}

}
