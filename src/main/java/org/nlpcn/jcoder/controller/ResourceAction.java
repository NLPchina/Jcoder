package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.ResourceService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.*;
import org.nutz.http.Response;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Stream;

@IocBean
@At("/admin/resource")
@Filters(@By(type = AuthoritiesManager.class))
@Ok("json")
@Fail("http:500")
public class ResourceAction {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceAction.class);

	@Inject
	private ResourceService resourceService;

	@Inject
	private ProxyService proxyService;

	@At
	public Restful list(@Param("groupName") String groupName, @Param("path") String path) {
		JSONArray jsonArray = new JSONArray();
		try {
			resourceService.getResourceFiles(jsonArray,groupName,path,"0");
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.ERR.msg(e.getMessage());
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name",groupName);
		jsonObject.put("id",0);
		jsonObject.put("open",true);
		jsonObject.put("iconOpen","modules/resource/css/zTreeStyle/img/diy/1_open.png");
		jsonObject.put("iconClose","modules/resource/css/zTreeStyle/img/diy/1_close.png");
		jsonArray.add(jsonObject);
		return Restful.OK.obj(jsonArray);
	}

	@At
	@Ok("void")
	public void downFile(@Param("groupName") String groupName, @Param("path") String path, HttpServletResponse response){
		try {
			FileInfo fileInfo = resourceService.getFileInfo(groupName, path);
			ByteArrayInputStream bais = new ByteArrayInputStream(fileInfo.getMd5().getBytes("utf-8"));
			response.addHeader("Content-Disposition", "attachment;filename=" +
					URLEncoder.encode(fileInfo.getName(), "utf-8"));
			response.setContentType("application/octet-stream");
			IOUtil.writeAndClose(bais,response.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@At
	public Restful createFolder(@Param("hostPorts") String[] hostPorts,@Param("groupName") String groupName,
							 @Param("path") String path, @Param("folderName") String folderName,
							 @Param(value = "first", df = "true") boolean first){
		try {
			if(!first){
				resourceService.createFolder(groupName,path,folderName);
				return Restful.instance().ok(true).msg("保存成功！");

			}else{
				Set<String> hostPortsArr = new HashSet<>();
				Arrays.stream(hostPorts).forEach(s -> hostPortsArr.add((String) s));
				String message = proxyService.post(hostPortsArr, "/admin/resource/createFolder",
						ImmutableMap.of("groupName", groupName,"path",path,"folderName",folderName,"first", false), 100000,
						ProxyService.MERGE_MESSAGE_CALLBACK);
				resourceService.createFolder2ZK(groupName,path,folderName);
				return Restful.instance().ok(true).msg(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.instance().ok(false).msg("保存失败！" + e.getMessage());
		}
	}

	@At
	@AdaptBy(type = UploadAdaptor.class)
	public Restful uploadFile(@Param("hostPorts") String[] hostPorts, @Param("group_name") String groupName,
							 @Param("file") TempFile[] file, @Param("fileNames") String[] fileNames,
							  @Param(value = "first", df = "true") boolean first) throws IOException {
		int fileNum = (int) Stream.of(file).filter(f -> f.getSubmittedFileName().toLowerCase().endsWith(".jar")).count();

		if (fileNum <= 0) {
			LOG.warn(" not find any jar file!");
		}

		JarService jarService = JarService.getOrCreate(groupName) ;
		try {
			if(!first){
				for (int i = 0; i < fileNames.length; i++) {
					String fileName = fileNames[i];
					if (fileName.toLowerCase().endsWith(".jar")) {
						try {
							File to = new File(jarService.getJarPath()+"/" + fileName);
							file[i].write(to.getAbsolutePath());
							LOG.info("write file to " + to.getAbsolutePath());
							fileNum++;
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						LOG.warn(fileName + " not a jar ! so skip it!");
					}
				}
				JarService.remove(groupName);
			}else{
				Set<String> hostPortsArr = new HashSet<>();
				Arrays.stream(hostPorts).forEach(s -> hostPortsArr.add((String) s));
				File[] files = Arrays.stream(file).map(f -> f.getFile()).toArray(File[]::new) ;
				String[] fns = Arrays.stream(file).map(f -> f.getSubmittedFileName()).toArray(String[]::new) ;
				proxyService.upload(hostPortsArr, "/admin/jar/uploadJar",ImmutableMap.of("group_name",groupName,"file",files,
						"fileNames",fns,"first",false) , 100000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.instance().ok(false).msg("保存失败！" + e.getMessage());
		}
		return Restful.instance().ok(true).msg("upload " + fileNum + " file ok!");
	}
}
