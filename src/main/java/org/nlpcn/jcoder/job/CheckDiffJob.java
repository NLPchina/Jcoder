package org.nlpcn.jcoder.job;

import com.google.common.collect.Sets;
import org.nlpcn.jcoder.domain.Different;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.GroupCache;
import org.nlpcn.jcoder.service.FileInfoService;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 定时比和主集群做different
 */
public class CheckDiffJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(CheckDiffJob.class);

	/**
	 * 存储了不同
	 */
	private static final Map<String, HashSet<String>> DIFF_MAP = new HashMap<>();

	@Override
	public void run() {

		while (true) {
			LOG.info("to run different !");
			try {
				for (Group group : GroupService.allLocalGroup()) {
					String name = group.getName();
					HashSet<String> paths = DIFF_MAP.get(name);
					if (paths == null || paths.size() == 0) {
						FileInfo root = StaticValue.space().getDataInGroupCache(SharedSpaceService.GROUP_PATH + "/" + name + "/file", FileInfo.class);
						GroupCache groupCache = FileInfoService.getGroupCache(name);
						if (groupCache != null && root != null && root.getMd5().equals(groupCache.getGroupMD5())) {
							LOG.info(name + " file md5 same so skip diff");
						} else {
							StaticValue.space().joinCluster(group, false);
						}
					} else {
						Set<String> taskNames = null;
						Set<String> relativePaths = null;
						List<String> rm = new ArrayList<>() ;
						for (String path : paths) {
							if (path.startsWith("/")) {
								relativePaths = Sets.newHashSet(path);
							} else {
								taskNames = Sets.newHashSet(path);
							}
							List<Different> different = StaticValue.space().different(group.getName(), taskNames, relativePaths, false);

							if (different.size() == 0) {
								rm.add(path);
							} else {
								break;
							}
						}

						if(rm.size()>0){
							paths.removeAll(rm);
							if(paths.size()==0){
								StaticValue.space().joinCluster(group, false);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(60 * 1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 只要发现不同就往这里扔
	 *
	 * @param differents
	 */
	public static void addDiff(String groupName, List<Different> differents) {
		if (differents == null || differents.size() == 0) {
			return;
		}
		HashSet<String> paths = DIFF_MAP.computeIfAbsent(groupName, (k) -> new HashSet<>());
		for (Different different : differents) {
			paths.add(different.getPath());
		}
	}

}
