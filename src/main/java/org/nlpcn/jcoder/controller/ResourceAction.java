package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSONArray;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.ResourceService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@IocBean
@At("/admin/resource")
@Filters(@By(type = AuthoritiesManager.class))
@Ok("json")
@Fail("http:500")
public class ResourceAction {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceAction.class);

	@Inject
	private ResourceService resourceService;

	@At
	public Restful list(@Param("groupName") String groupName, @Param("path") String path) throws Exception {
		JSONArray jsonArray = new JSONArray();
		//PathChildrenCache groupCache = new PathChildrenCache();
		if (StringUtil.isBlank(path)) {
			path = "/file";
		} else {
			path = "/file" + path;
		}

		List<String> strings = StaticValue.space().getZk().getChildren().forPath(SharedSpaceService.GROUP_PATH + "/" + groupName + path);
		for (String s : strings) {
			byte[] data2ZK = StaticValue.space().getData2ZK(SharedSpaceService.GROUP_PATH + "/" + groupName + path + "/" + s);
			if (data2ZK == null || data2ZK.length == 0) {
				jsonArray.add(s);
			}
		}
		return Restful.ok().obj(jsonArray);
		/*try {
			JSONArray jsonArray = new JSONArray();

            Set<String> set = new HashSet<>() ;
            StaticValue.space().walkDataNode(set , SharedSpaceService.GROUP_PATH+"/"+groupName+"/file");
			//StaticValue.space().getZk().getData().forPath("/jcoder/group/"+ groupName + "/file");

			//List<String> strings = StaticValue.space().getZk().getChildren().forPath("/jcoder/group/"+ groupName + "/file/resources");
			return Restful.OK.obj(jsonArray);
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.ERR.msg(e.getMessage());
		}*/
	}

}
