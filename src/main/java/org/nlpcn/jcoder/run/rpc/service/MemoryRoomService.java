package org.nlpcn.jcoder.run.rpc.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 单机版的WsRoom实现
 *
 * Created by Ansj on 24/01/2018.
 */
public class MemoryRoomService implements RoomService {

	protected ConcurrentHashMap<String, ConcurrentSkipListSet<String>> rooms = new ConcurrentHashMap<>();


	public void join(String room, String id) {
		ids(room).add(id);
	}

	public void left(String room, String id) {
		ids(room).remove(id);
	}

	public Set<String> ids(String room) {
		ConcurrentSkipListSet<String> _room = rooms.get(room);
		if (_room == null) {
			_room = new ConcurrentSkipListSet<String>();
			ConcurrentSkipListSet<String> prev = rooms.putIfAbsent(room, _room);
			if (prev != null)
				_room = prev;
		}
		return _room;
	}

	public Collection<String> getRoomNames() {
		return new ArrayList<>(rooms.keySet());
	}

	@Override
	public void sendMessage(String room, String message) {
		List<String> remove = new ArrayList<>();
		ids(room).stream().forEach((String id) -> {
			Optional.of(SessionService.getRpcUser(id)).ifPresent(v -> {
				if (v.getSession().isOpen()) {
					if (message != null)
						v.getSession().getAsyncRemote().sendText(message);
				} else {
					remove.add(id);
				}
			});
		});

		remove.parallelStream().forEach(id -> left(room, id));
	}

	@Override
	public void dropRoom(String room, boolean deleteChild) {
		Set<String> ids = ids(room);
		if (ids.size() > 0 && !deleteChild) {
			return;
		} else {
			rooms.remove(room);
		}
	}

	@Override
	public void close() throws IOException {

	}
}
