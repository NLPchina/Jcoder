package org.nlpcn.jcoder.job;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.scheduler.TaskException;
import org.nlpcn.jcoder.service.H2Server;
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
	}

	@Override
	public void init(NutConfig nc) {

		LOG.info("init begin ! ");
		// 设置ioc

		StaticValue.setSystemIoc(nc.getIoc());

		H2Server.startServer(nc);
		

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

		// task 检查集群中其他机器的运行状况
		new Thread(new CheckTaskJob()).start();

		LOG.info("begin run task quene!");
		// 运行队列
		new Thread(new RunTaskJob()).start();

		LOG.info("begin print console job!");
		// 运行日志打印到websocket任务
		new Thread(new PrintConsoleJob()).start();

	}

}
