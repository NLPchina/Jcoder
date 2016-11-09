package org.nlpcn.jcoder.server.rpc.server;

import java.beans.Transient;
import java.util.Set;

import com.google.common.collect.Sets;

import io.netty.channel.Channel;

/**
 * package mobile channel
 * 
 * @author Ansj
 *
 */
public class ClientChannel {

	private String clientId;

	private Channel channel;

	private long loginTime;
	
	private long lastHeartTime ;

	public ClientChannel(String clientId,Channel channel) {
		this.clientId = clientId ;
		this.channel = channel ;
		this.loginTime = System.currentTimeMillis();
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public long getLastHeartTime() {
		return lastHeartTime;
	}

	public void setLastHeartTime(long lastHeartTime) {
		this.lastHeartTime = lastHeartTime;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

}
