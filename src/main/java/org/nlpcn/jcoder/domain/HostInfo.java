package org.nlpcn.jcoder.domain;

import java.io.Serializable;

/**
 * 主机信息序列化后存放于集群中
 */
public class HostInfo implements Serializable{
	private String host ;
	private int port ;
	private int weight ;
	private boolean isSsl ;
	private String md5 ;
	private boolean current ;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public boolean isSsl() {
		return isSsl;
	}

	public void setSsl(boolean ssl) {
		isSsl = ssl;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}
}
