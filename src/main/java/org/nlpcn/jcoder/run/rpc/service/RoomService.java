package org.nlpcn.jcoder.run.rpc.service;

import java.io.Closeable;
import java.util.Collection;
import java.util.Set;

/**
 * Created by Ansj on 24/01/2018.
 */
public interface RoomService extends Closeable {

	Set<String> ids(String room);

	void join(String room, String id);

	void left(String room, String id);

	Collection<String> getRoomNames();


	/**
	 * 发送消息给房间所有人
	 *
	 * @param room
	 * @param message
	 */
	void sendMessage(String room, final String message);

	/**
	 * 删除一个房间
	 *
	 * @param room        房间名称
	 * @param deleteChild true 如果房间内有人也一并删除。 false 房间有人就不删了
	 */
	void dropRoom(String room, boolean deleteChild);
}
