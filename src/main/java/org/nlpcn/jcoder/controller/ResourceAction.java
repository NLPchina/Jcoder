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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

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
	public Restful list(@Param("groupName") String groupName) {
		try {

			Set<String> set = new HashSet<>() ;
			StaticValue.space().walkAllDataNode(set , SharedSpaceService.GROUP_PATH+"/"+groupName+"/file");

			for (String s : set) {
				FileInfo fileInfo = StaticValue.space().getData(s, FileInfo.class) ;
			}


			List<String> strings = StaticValue.space().getZk().getChildren().forPath("/jcoder/group/"+ groupName + "/file/resources");
			return Restful.OK.obj(strings);
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.ERR.msg(e.getMessage());
		}
	}

}
