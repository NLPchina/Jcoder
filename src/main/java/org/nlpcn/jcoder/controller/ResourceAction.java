package org.nlpcn.jcoder.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.javaparser.ast.internal.Utils;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.eclipse.jdt.internal.compiler.classfmt.FieldInfo;
import org.nlpcn.jcoder.service.ResourceService;

import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.upload.FieldMeta;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@IocBean
@At("/admin/resource")
@Filters(@By(type = AuthoritiesManager.class))
@Ok("json")
@Fail("http:500")
public class ResourceAction {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceAction.class) ;

	@Inject
	private ResourceService resourceService;

	@At
	public Restful list(@Param("groupName") String groupName , @Param("path") String path) throws Exception {
		JSONArray jsonArray = new JSONArray();
		//PathChildrenCache groupCache = new PathChildrenCache();
		if(StringUtil.isBlank(path)){
			path = "/file" ;
		}else{
			path = "/file"+path ;
		}

		List<String> strings = StaticValue.space().getZk().getChildren().forPath(SharedSpaceService.GROUP_PATH + "/" + groupName+path);
		for (String s: strings) {
			byte[] data2ZK = StaticValue.space().getData2ZK(SharedSpaceService.GROUP_PATH + "/" + groupName+path+"/"+s);
			if(data2ZK == null || data2ZK.length == 0){
				jsonArray.add(s);
			}
		}
		return Restful.OK.obj(jsonArray);
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
