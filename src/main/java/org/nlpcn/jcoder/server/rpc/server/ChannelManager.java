package org.nlpcn.jcoder.server.rpc.server;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.nlpcn.jcoder.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

public class ChannelManager {

	private static final Logger LOG = LoggerFactory.getLogger(ChannelManager.class);

	private static final ConcurrentHashMap<Channel, ClientChannel> CHANNEL_MAP = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, ClientChannel> CLIENT_ID_MAP = new ConcurrentHashMap<>();

	private static final ReentrantLock LOCK = new ReentrantLock();

	/**
	 * get all channel
	 * 
	 * @return
	 */
	public Collection<ClientChannel> getChannels() {
		return CLIENT_ID_MAP.values();
	}

	/**
	 * remove a channel
	 * 
	 * @param channel
	 */
	public static void remove(Channel channel) {

		try {
			LOCK.lock();
			ClientChannel clientChannel = CHANNEL_MAP.remove(channel);
			if (clientChannel != null) {
				CLIENT_ID_MAP.remove(clientChannel.getClientId());
			}
		} finally {
			LOCK.unlock();
		}
	}

	/**
	 * add a channel
	 * 
	 * @param channel
	 */
	public static void add(String clientId, Channel channel) {

		try {
			LOCK.lock();
			ClientChannel clientChannel = null;

			if ((clientChannel = CLIENT_ID_MAP.get(clientId)) == null) {
				clientChannel = new ClientChannel(clientId, channel);
				CLIENT_ID_MAP.put(clientId, clientChannel);
				CHANNEL_MAP.put(channel, clientChannel);
			}
		} finally {
			LOG.info(clientId + " login " + DateUtils.formatDate(new Date(), DateUtils.SDF_STANDARD));
			LOCK.unlock();
		}

	}
}
