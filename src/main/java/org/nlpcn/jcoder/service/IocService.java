package org.nlpcn.jcoder.service;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import org.apache.zookeeper.data.Stat;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.util.IOUtil;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.domain.HostGroup;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean
public class IocService {
	private SharedSpaceService sharedSpaceService;

	public IocService() {
		this.sharedSpaceService = StaticValue.space();
	}

	public Map<String,HostGroup> getAllHosts(final String groupName) throws Exception {

		Map<String,HostGroup> result = new HashMap<>() ;

		Set<Map.Entry<String, HostGroup>> entries = sharedSpaceService.getHostGroupCache().entrySet();

		for (Map.Entry<String, HostGroup> entry : entries) {
			String[] split = entry.getKey().split("_");

			if(split[1].equals(groupName)){
				result.put(split[0],entry.getValue()) ;
			}
		}
		return result;
	}

	public String getIocInfo(String groupName) throws Exception {
		byte[] data2ZK = sharedSpaceService.getData2ZK(sharedSpaceService.GROUP_PATH +"/"+ groupName + "/file/resources/ioc.js");
		if(data2ZK == null)return "";
        FileInfo fileInfo = JSONObject.parseObject(data2ZK, FileInfo.class);
        return fileInfo.getMd5() ;
	}

	public void saveIocInfo(String groupName,String code) throws Exception {
		FileInfo fileInfo = new FileInfo() ;
        fileInfo.setFile(new File(StaticValue.GROUP_FILE,groupName+"/resources/ioc.js"));
		fileInfo.setMd5(code);
		fileInfo.setDirectory(false);
		fileInfo.setLength(code.getBytes("utf-8").length);
		fileInfo.setName("ioc.js");
		fileInfo.setRelativePath("resources/ioc.js");

		sharedSpaceService.getZk().setData().forPath(sharedSpaceService.GROUP_PATH+"/"+groupName+"/file/resources/ioc.js", JSONObject.toJSONBytes(fileInfo));

	}
}
