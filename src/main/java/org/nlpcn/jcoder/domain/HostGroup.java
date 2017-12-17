package org.nlpcn.jcoder.domain;

import java.io.Serializable;

/**
 * 主机信息序列化后存放于集群中
 */
public class HostGroup implements Serializable{
	private int weight ;
	private boolean ssl ;
	private boolean current ;
	private String hostPort ;

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	public String getHostPort() {
		return hostPort;
	}

	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}
}
