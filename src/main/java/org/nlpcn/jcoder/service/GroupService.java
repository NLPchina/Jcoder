package org.nlpcn.jcoder.service;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.nlpcn.jcoder.domain.Different;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.HostGroup;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskHistory;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.nlpcn.jcoder.service.SharedSpaceService.GROUP_PATH;
import static org.nlpcn.jcoder.util.StaticValue.GROUP_FILE;
import static org.nlpcn.jcoder.util.StaticValue.getHostPort;
import static org.nlpcn.jcoder.util.StaticValue.space;
import static org.nlpcn.jcoder.util.StaticValue.systemDao;

/**
 * Created by Ansj on 05/12/2017.
 */
@IocBean
public class GroupService {

	private static final Logger LOG = LoggerFactory.getLogger(GroupService.class);

	@Inject
	private TaskService taskService;

	private BasicDao basicDao = systemDao;


	public List<Group> list() throws Exception {
		List<Group> result = new ArrayList<>();

		getAllGroupNames().forEach((String gName) -> {
			Group group = new Group();
			group.setName(gName);
			try {
				Set<String> children = space().getGroupCache().getCurrentChildren(GROUP_PATH + "/" + gName).keySet();
				group.setTaskNum(children.size() - 1);

				Set<String> set = new HashSet<>();
				space().walkGroupCache(set, GROUP_PATH + "/" + gName + "/file");
				group.setFileNum(set.size());

				FileInfo root = space().getDataInGroupCache(GROUP_PATH + "/" + gName + "/file", FileInfo.class);

				group.setFileLength(root.getLength());

				Set<Map.Entry<String, HostGroup>> entries = space().getHostGroupCache().entrySet();

				List<HostGroup> hosts = new ArrayList<>();

				for (Map.Entry<String, HostGroup> entry : entries) {
					String[] split = entry.getKey().split("_");
					if (split.length == 1) {
						continue;
					}
					if (gName.equals(split[1])) {
						HostGroup hg = entry.getValue();
						hg.setHostPort(split[0]);
						hosts.add(hg);
					}
				}

				group.setHosts(hosts);
				result.add(group);

			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		result.sort(Comparator.comparingInt(g -> -g.getHosts().size()));

		return result;
	}

	/**
	 * 得到一个group下所有主机的信息
	 */
	public List<HostGroup> getGroupHostList(String groupName) throws Exception {

		Set<Map.Entry<String, HostGroup>> entries = space().getHostGroupCache().entrySet();

		List<HostGroup> hosts = new ArrayList<>();
		for (Map.Entry<String, HostGroup> entry : entries) {
			String[] split = entry.getKey().split("_");
			if (split.length == 1) {
				continue;
			}
			if (groupName.equals(split[1])) {
				HostGroup hg = entry.getValue();
				hg.setHostPort(split[0]);
				hosts.add(hg);
			}
		}

		return hosts;
	}

	/**
	 * 获取所有的分组
	 */
	public List<String> getAllGroupNames() throws Exception {
		return new ArrayList<>(space().getGroupCache().getCurrentChildren(GROUP_PATH).keySet());
	}

	public List<String> getAllHosts() throws Exception {
		return space().getAllHosts();
	}


	/**
	 * 从集群把一个组彻底删除掉
	 */
	public void deleteByCluster(String groupName) throws Exception {

		InterProcessMutex interProcessMutex = space().lockGroup(groupName);

		try {
			interProcessMutex.acquire();
			//判断当前是否有机器在使用此group
			for (String k : space().getHostGroupCache().keySet()) {
				String gName = k.split("_")[1];
				if (gName.equals(groupName)) {
					throw new Exception("组: " + groupName + "存在主机持有。清删除后重试: " + k);
				}
			}
			space().getZk().delete().deletingChildrenIfNeeded().forPath(SharedSpaceService.GROUP_PATH + "/" + groupName);
		} finally {
			space().unLockAndDelete(interProcessMutex);
		}
	}

	public boolean deleteGroup(String name) {

		JarService.getOrCreate(name).release(); //释放环境变量

		Group group = findGroupByName(name);

		if (group != null) {
			basicDao.delById(group.getId(), Group.class);

			List<Task> tasks = taskService.findTasksByGroupName(group.getName());

			for (Task task : tasks) {
				try {
					LOG.info("delete task " + task.getName());
					taskService.delete(task);
					taskService.delByDB(task);
					basicDao.delByCondition(TaskHistory.class, Cnd.where("taskId", "=", task.getId()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		String key = getHostPort() + "_" + name;

		Files.deleteFile(new File(GROUP_FILE, name + ".cache"));

		File groupFile = new File(GROUP_FILE, name);

		Files.deleteDir(groupFile);

		/**
		 * 尝试循环删除
		 */
		for (int i = 0; i < 10 && groupFile.exists(); i++) {
			Files.deleteDir(groupFile);
			System.gc();
			LOG.info("delete group:{} times:{}", name, i + 1);
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		HostGroup hostGroup = space().getHostGroupCache().get(key);
		if (hostGroup != null && !groupFile.exists()) {
			hostGroup.setWeight(-1); //理论上设置为-1就删除了
			space().getHostGroupCache().put(key, hostGroup);
			space().getHostGroupCache().remove(key);
			LOG.info("remove host_group in zk : " + hostGroup.getHostPort());
		}


		return !groupFile.exists();


	}

	public Group findGroupByName(String name) {
		LOG.info("find group by db " + name);
		return basicDao.findByCondition(Group.class, Cnd.where("name", "=", name));
	}


	/**
	 * 刷新一个group重新加载到集群中
	 */
	public List<Different> flush(String groupName, boolean upMapping) throws IOException {
		Group group = findGroupByName(groupName);
		if (group == null) {
			throw new RuntimeException("group not found " + groupName);
		}
		return flush(group, upMapping);
	}

	/**
	 * 刷新
	 */
	public List<Different> flush(Group group, boolean upMapping) throws IOException {
		return space().joinCluster(group, upMapping);
	}

	public static List<Group> allLocalGroup() {
		LOG.info("find all group by db");
		return systemDao.search(Group.class, "id");

	}

	/**
	 * 随机的获取一台和主版本同步着的主机
	 */
	public List<String> getCurrentHostPort(String groupName) {
		List<String> collect = StaticValue.space().getHostGroupCache().entrySet().stream().filter(e -> e.getValue().isCurrent()).map(e -> e.getKey()).filter(k -> groupName.equals(k.split("_")[1])).map(k -> k.split("_")[0]).collect(Collectors.toList());
		return collect;
	}

	/**
	 * 从主机集群中获取随机一个同步版本的机器，如果机器不存在则返回null
	 *
	 * @param groupName 组名称
	 */
	public String getRandomCurrentHostPort(String groupName) {
		List<String> collect = getCurrentHostPort(groupName);
		if (collect.size() == 0) {
			return null;
		}
		return collect.get(new Random().nextInt(collect.size()));
	}
}
