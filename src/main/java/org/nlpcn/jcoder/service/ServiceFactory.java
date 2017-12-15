package org.nlpcn.jcoder.service;

import org.nlpcn.jcoder.service.impl.ClusterGroupService;
import org.nlpcn.jcoder.service.impl.LocalGroupSerivce;
import org.nlpcn.jcoder.util.StaticValue;

public class ServiceFactory {

	public static GroupService createGroupService() {
		if (StaticValue.IS_LOCAL) {
			return new LocalGroupSerivce();
		} else {
			return new ClusterGroupService(StaticValue.space());
		}
	}

}
