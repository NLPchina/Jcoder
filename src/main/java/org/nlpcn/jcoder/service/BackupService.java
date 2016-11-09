package org.nlpcn.jcoder.service;

import static org.nlpcn.jcoder.util.StaticValue.systemDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@IocBean
public class BackupService {

	private TaskService taskService = StaticValue.getBean(TaskService.class, "taskService");

	public String backup() {
		StringBuilder sb = new StringBuilder();

		List<Group> groups = systemDao.search(Group.class, "id");

		groups.forEach(g -> {
			JSONObject job = (JSONObject) JSONObject.toJSON(g);
			List<Task> tasks = systemDao.search(Task.class, Cnd.where("groupId", "=", g.getId()));
			job.put("tasks", tasks);
			sb.append(job.toJSONString());
			sb.append("\n");
		});

		return sb.toString();
	}

	public String restore(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(reader)) {
			String temp = null;
			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					continue;
				}
				JSONObject job = JSONObject.parseObject(temp);
				JSONArray jarrs = (JSONArray) job.remove("tasks");

				if (jarrs == null) {
					continue;
				}

				Group group = JSONObject.toJavaObject(job, Group.class);
				//查看此group是否存在
				List<Group> search = systemDao.search(Group.class, Cnd.where("name", "=", group.getName()));
				if (search != null && search.size() > 0) { //说明存在
					group = search.get(0);
				} else {
					group.setId(null);
					try {
						systemDao.save(group);
						sb.append("create group " + group.getName() + "\n");
					} catch (Exception e) {
						sb.append("err create group " + group.getName() + "\n");
						e.printStackTrace();
					}
				}

				long groupId = group.getId();

				jarrs.stream().map(j -> JSONObject.toJavaObject((JSONObject) j, Task.class)).forEach(t -> {
					t.setGroupId(groupId);
					//判断task是否存在
					List<Task> tasks = systemDao.search(Task.class, Cnd.where("name", "=", t.getName()));
					if (tasks == null || tasks.size() == 0) {
						try {
							systemDao.save(t);
							sb.append("create task " + t.getName() + "\n");
						} catch (Exception e) {
							e.printStackTrace();
							sb.append("err create task " + t.getName() + "\n");
						}
					} else {
						t.setId(tasks.get(0).getId());

						try {
							systemDao.update(t);
							sb.append("update task " + t.getName() + "\n");
						} catch (Exception e) {
							e.printStackTrace();
							sb.append("err update task " + t.getName() + "\n");
						}
					}
				});

			}
		}

		try {
			if (sb.length() > 0) {
				taskService.initTaskFromDB();
			}
		} catch (Exception e) {
			e.printStackTrace();
			sb.append("err check all task " + e.getMessage());
		}

		return sb.toString();

	}
}
