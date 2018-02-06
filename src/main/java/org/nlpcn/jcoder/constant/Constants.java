package org.nlpcn.jcoder.constant;

public interface Constants {

	/**
	 * 内部接口访问超时时间
	 */
	int TIMEOUT = 100000;

	/**
	 * master主机标识
	 */
	String HOST_MASTER = "master";

	/**
	 * 请求接口参数是否是debug
	 */
	String DEBUG = "_debug";

	/**
	 * master主机任务ID
	 */
	long MASTER_TASK_ID = 0L;


	/**
	 * 定时任务，group和task的分隔符
	 */
	String GROUP_TASK_SPLIT = "@";


	/**
	 * 代理请求头，如果有这个头，则不代理
	 */
	String PROXY_HEADER = "_proxy";

	/**
	 * 内置房间的log
	 */
	String LOG_ROOM = "_jcoder_log" ;
}
