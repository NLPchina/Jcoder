package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.List;

import static org.nlpcn.jcoder.util.StaticValue.systemDao;

@IocBean
public class BackupService {

	private TaskService taskService = StaticValue.getSystemIoc().get(TaskService.class, "taskService");

	public String backup() {
		StringBuilder sb = new StringBuilder();

		List<Group> groups = systemDao.search(Group.class, "id");

		groups.forEach(g -> {
			JSONObject job = (JSONObject) JSONObject.toJSON(g);
			List<Task> tasks = systemDao.search(Task.class, Cnd.where("groupName", "=", g.getName()));
			job.put("tasks", tasks);
			sb.append(job.toJSONString());
			sb.append("\n");
		});

		return sb.toString();
	}

}
