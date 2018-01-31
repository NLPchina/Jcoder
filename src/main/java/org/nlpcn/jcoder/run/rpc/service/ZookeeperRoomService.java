package org.nlpcn.jcoder.run.rpc.service;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.ZookeeperDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Ansj on 24/01/2018.
 */
public class ZookeeperRoomService implements RoomService {

	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperRoomService.class);

	private static final byte[] EMPTY = new byte[0];

	private static final String ROOM_PATH = StaticValue.ZK_ROOT + "/room/" + StaticValue.getHostPort();

	private TreeCache roomCache = null;

	private ZookeeperDao zkDao;


	public ZookeeperRoomService(ZookeeperDao zkDao) throws Exception {
		this.zkDao = zkDao;
		this.zkDao.getZk().createContainers(ROOM_PATH);
		roomCache = new TreeCache(this.zkDao.getZk(), ROOM_PATH).start();
	}

	@Override
	public Set<String> ids(String room) {
		Map<String, ChildData> currentChildren = roomCache.getCurrentChildren(ROOM_PATH + "/" + room);
		if (currentChildren == null) {
			return Collections.emptySet();
		}
		return currentChildren.keySet();
	}

	@Override
	public void join(String room, String id) {
		try {
			String path = ROOM_PATH + "/" + room + "/" + id;

			boolean flag = true;
			if (zkDao.getZk().checkExists().forPath(path) == null) {
				zkDao.getZk().create().creatingParentsIfNeeded().forPath(path);
			}
			zkDao.getZk().setData().forPath(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void left(String room, String id) {
		try {
			zkDao.getZk().delete().forPath(ROOM_PATH + "/" + room + "/" + id);
		} catch (KeeperException.NoNodeException e) {
			LOG.warn("left home {} id {} is no node", room, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Collection<String> getRoomNames() {
		return roomCache.getCurrentChildren(ROOM_PATH).keySet();
	}

	@Override
	public void close() throws IOException {
		if (roomCache != null) {
			roomCache.close();
		}
	}

	@Override
	public void dropRoom(String room, boolean deleteChild) {

		ChildData currentData = roomCache.getCurrentData(ROOM_PATH + "/" + room);

		if (currentData == null) {
			return;
		}

		Stat stat = currentData.getStat();

		if (stat.getNumChildren() > 0 && !deleteChild) {
			return;
		} else {
			try {
				zkDao.getZk().delete().deletingChildrenIfNeeded().forPath(ROOM_PATH + "/" + room);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void sendMessage(String room, final String message) {
		this.ids(room).parallelStream().forEach((String id) -> {
			Optional.of(SessionService.getRpcUser(id)).ifPresent(v -> {
				if (v.getSession().isOpen()) {
					if (message != null) {
						v.getSession().getAsyncRemote().sendText(message);
					}
				} else {
					left(room, id);
				}
			});
		});
	}

}
