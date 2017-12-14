package org.nlpcn.jcoder.service;

import static org.nlpcn.jcoder.util.StaticValue.systemDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@IocBean
public class BackupService {

	private TaskService taskService = StaticValue.getSystemIoc().get(TaskService.class, "taskService");

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

}
