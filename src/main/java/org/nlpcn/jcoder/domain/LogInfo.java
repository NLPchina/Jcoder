package org.nlpcn.jcoder.domain;

import org.apache.logging.log4j.core.LogEvent;
import org.nlpcn.jcoder.run.rpc.Rpcs;

/**
 * Created by Ansj on 06/02/2018.
 */
public class LogInfo {

	private String threadName;

	private String groupName;

	private String className;

	private String methodName;

	private String message;

	private long time ;

	public LogInfo(LogEvent event) {
		this.threadName = event.getThreadName();
		this.groupName = Rpcs.ctx().getGroupName();
		if (groupName != null) {
			this.className = Rpcs.ctx().getClassName();
			this.methodName = Rpcs.ctx().getMethodName();
		}
		this.message = event.getMessage().getFormattedMessage();
		this.time = event.getTimeMillis() ;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
