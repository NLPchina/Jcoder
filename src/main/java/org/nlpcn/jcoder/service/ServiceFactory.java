package org.nlpcn.jcoder.service;

import org.nlpcn.jcoder.service.impl.ClusterGroupService;
import org.nlpcn.jcoder.util.StaticValue;

public class ServiceFactory {

	/**
	 * 创建groupservice
	 * @return
	 */
	public static GroupService createGroupService() {
		if (StaticValue.IS_LOCAL) {
//			return new LocalGroupSerivce();
			return null ;
		} else {
			return new ClusterGroupService(StaticValue.space());
		}
	}

}
