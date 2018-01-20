package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;

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
import java.util.Set;

import static org.nlpcn.jcoder.service.SharedSpaceService.GROUP_PATH;

/**
 * Created by Ansj on 05/12/2017.
 */
@IocBean
public class GroupService {

	private static final Logger LOG = LoggerFactory.getLogger(GroupService.class);

	@Inject
	private TaskService taskService;

	private BasicDao basicDao = StaticValue.systemDao;

	private SharedSpaceService sharedSpaceService = StaticValue.space();

	public List<Group> list() throws Exception {
		List<Group> result = new ArrayList<>();

		getAllGroupNames().forEach((String gName) -> {
			Group group = new Group();
			group.setName(gName);
			try {
				List<String> children = sharedSpaceService.getZk().getChildren().forPath(GROUP_PATH + "/" + gName);
				group.setTaskNum(children.size() - 1);

				Set<String> set = new HashSet<>();
				sharedSpaceService.walkAllDataNode(set, GROUP_PATH + "/" + gName + "/file");
				group.setFileNum(set.size());

				FileInfo root = JSONObject.parseObject(sharedSpaceService.getData2ZK(GROUP_PATH + "/" + gName + "/file"), FileInfo.class);

				group.setFileLength(root.getLength());

				Set<Map.Entry<String, HostGroup>> entries = sharedSpaceService.getHostGroupCache().entrySet();

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

		Set<Map.Entry<String, HostGroup>> entries = sharedSpaceService.getHostGroupCache().entrySet();

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
		return sharedSpaceService.getZk().getChildren().forPath(GROUP_PATH);
	}

	public List<String> getAllHosts() throws Exception {
		return sharedSpaceService.getAllHosts();
	}


	/**
	 * 从集群把一个组彻底删除掉
	 */
	public void deleteByCluster(String groupName) throws Exception {

		InterProcessMutex interProcessMutex = sharedSpaceService.lockGroup(groupName);

		try {
			interProcessMutex.acquire();
			//判断当前是否有机器在使用此group
			for (String k : sharedSpaceService.getHostGroupCache().keySet()) {
				String gName = k.split("_")[1];
				if (gName.equals(groupName)) {
					throw new Exception("组: " + groupName + "存在主机持有。清删除后重试: " + k);
				}
			}
			sharedSpaceService.getZk().delete().deletingChildrenIfNeeded().forPath(SharedSpaceService.GROUP_PATH + "/" + groupName);
		} finally {
			sharedSpaceService.unLockAndDelete(interProcessMutex);
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

		String key = StaticValue.getHostPort() + "_" + name;

		Files.deleteFile(new File(StaticValue.GROUP_FILE, name + ".cache"));

		File grouFile = new File(StaticValue.GROUP_FILE, name);

		Files.deleteDir(grouFile);

		/**
		 * 尝试循环删除
		 */
		for (int i = 0; i < 10 && grouFile.exists(); i++) {
			Files.deleteDir(grouFile);
			System.gc();
			LOG.info("delete group:{} times:{}", name, i + 1);
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				throw new RuntimeException(e) ;
			}
		}

		HostGroup hostGroup = sharedSpaceService.getHostGroupCache().get(key);
		if (hostGroup != null && !grouFile.exists()) {
			hostGroup.setWeight(-1); //理论上设置为-1就删除了
			sharedSpaceService.getHostGroupCache().put(key, hostGroup);
			sharedSpaceService.getHostGroupCache().remove(key);
			LOG.info("remove host_group in zk : " + hostGroup.getHostPort());
		}



		return !grouFile.exists();


	}

	public Group findGroupByName(String name) {
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
		return sharedSpaceService.joinCluster(group, upMapping);
	}

}
