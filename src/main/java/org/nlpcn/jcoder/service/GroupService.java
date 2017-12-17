package org.nlpcn.jcoder.service;

import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.HostGroup;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Ansj on 05/12/2017.
 */
@IocBean
public interface GroupService {
	List<Group> list() throws Exception;
	void save(Group group) ;
	void delete(Group group) ;
	public List<String> getAllGroupNames() throws Exception ;
	public Set<String> getAllHosts() throws Exception;
	public List<HostGroup> getGroupHostList(String groupName) throws Exception ;

}
