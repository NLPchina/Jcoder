package org.nlpcn.jcoder.domain;

import com.alibaba.fastjson.JSONObject;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.ZKMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ansj on 27/12/2017.
 */
public class HostGroupWatcher implements Watcher {


	private static final Logger LOG = LoggerFactory.getLogger(HostGroupWatcher.class);

	private HostGroup hostGroup;

	private boolean delete = false;

	public HostGroupWatcher(HostGroup hostGroup) {
		this.hostGroup = hostGroup;
	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getType() == Event.EventType.NodeDataChanged) {
			String key = event.getPath().substring(SharedSpaceService.HOST_GROUP_PATH.length() + 1);
			ZKMap<HostGroup> hostGroupCache = StaticValue.space().getHostGroupCache();
			HostGroup hg = hostGroupCache.get(key);
			if (StaticValue.getHostPort().equals(hg.getHostPort())) {
				if (hg != null && hg.getWeight() < 0) {
					this.delete = true;
					hostGroupCache.remove(key);
				} else {
					hostGroup = hg;
				}
			}
		} else if (event.getType() == Event.EventType.NodeDeleted) {
			if (!delete) {
				try {
					String key = event.getPath().substring(SharedSpaceService.HOST_GROUP_PATH.length() + 1);
					String hostPort = key.split("_")[0];
					if (StaticValue.getHostPort().equals(hostPort)) {
						LOG.info("I lost HostGroup so add it again " + event.getPath());
						StaticValue.space().setData2ZKByEphemeral(event.getPath(), JSONObject.toJSONBytes(hostGroup), this);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
