package org.nlpcn.jcoder.service;

import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.UserGroup;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Ansj on 05/12/2017.
 */
@IocBean
public class GroupService {

	private static final Logger LOG = LoggerFactory.getLogger(GroupService.class);

	private static final ConcurrentHashMap<Object, Task> GROUP_CACHE = new ConcurrentHashMap<>();

	private BasicDao basicDao = StaticValue.systemDao;


	/**
	 * 删除一个group
	 * @param group
	 * @return
	 */
	public void delete(Group group) {
		basicDao.delById(group.getId(), Group.class);
		LOG.info("del group:" + group.getName());
		Condition con = Cnd.where("groupId", "=", group.getId());
		int num = basicDao.delByCondition(UserGroup.class, con);

	}

	public void save(Group group) throws Exception {
		group.setCreateTime(new Date());
		basicDao.save(group);
		LOG.info("add group:" + group.getName());
	}
}
