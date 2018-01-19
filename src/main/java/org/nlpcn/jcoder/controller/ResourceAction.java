//package org.nlpcn.jcoder.controller;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.google.common.collect.ImmutableMap;
//import org.nlpcn.jcoder.constant.Constants;
//import org.nlpcn.jcoder.domain.FileInfo;
//import org.nlpcn.jcoder.domain.Group;
//import org.nlpcn.jcoder.filter.AuthoritiesManager;
//import org.nlpcn.jcoder.service.*;
//import org.nlpcn.jcoder.util.*;
//import org.nutz.http.Response;
//import org.nutz.ioc.loader.annotation.Inject;
//import org.nutz.ioc.loader.annotation.IocBean;
//import org.nutz.mvc.annotation.*;
//import org.nutz.mvc.upload.TempFile;
//import org.nutz.mvc.upload.UploadAdaptor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.*;
//import java.net.URLEncoder;
//import java.util.*;
//import java.util.stream.Stream;
//
//@IocBean
//@At("/admin/resource")
//@Filters(@By(type = AuthoritiesManager.class))
//@Ok("json")
//@Fail("http:500")
//public class ResourceAction {
//
//	private static final Logger LOG = LoggerFactory.getLogger(ResourceAction.class);
//
//	@Inject
//	private IocService iocService;
//
//	@Inject
//	private ResourceService resourceService;
//
//	@Inject
//	private ProxyService proxyService;
//
//	@At
//	public Restful saveAndFlush(@Param("hostPorts[]") String[] hostPorts,@Param("groupName") String groupName,
//						@Param("content") String content,
//						@Param("relativePath") String relativePath,
//						@Param(value = "first", df = "true") boolean first) {
//		try {
//			if(!first){
//				JarService jarService = JarService.getOrCreate(groupName) ;
//				if(relativePath.endsWith("ioc.js")){
//					jarService.saveIoc(groupName, content);
//				}else if(relativePath.endsWith("pom.xml")){
//					jarService.savePom(groupName, content);
//				}else{
//
//				}
//				return Restful.instance().ok(true).msg("保存并刷新成功！");
//			}else{
//				List<String> hosts = Arrays.asList(hostPorts);
//				Set<String> hostPortsArr = new HashSet<>(hosts);
//				Set<String> firstHost = new HashSet<String>();
//				if(hostPortsArr.contains(Constants.HOST_MASTER)){
//					hostPortsArr.remove(Constants.HOST_MASTER);
//					ArrayList arrayList = new ArrayList(hosts);
//					arrayList.remove(Constants.HOST_MASTER);
//					firstHost.add(arrayList.get(0).toString());
//				}
//				String message = proxyService.post(hostPortsArr, "/admin/resource/saveAndFlush",
//						ImmutableMap.of("groupName", groupName,"relativePath",relativePath,"content",content,"first", false), 100000,
//						ProxyService.MERGE_MESSAGE_CALLBACK);
//				//更新master数据节点
//				if(firstHost != null && firstHost.size() > 0){
//					proxyService.post(firstHost, "/admin/fileInfo/upCluster",
//							ImmutableMap.of("groupName",groupName,"relativePaths",
//									new String[]{relativePath}),100000);
//				}
//				return Restful.instance().ok(true).msg(message);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return Restful.instance().ok(false).msg("保存失败！" + e.getMessage());
//		}
//	}
//
//	@At
//	@AdaptBy(type = UploadAdaptor.class)
//	public Restful uploadFile(@Param("hostPorts") String[] hostPorts, @Param("group_name") String groupName,
//							  @Param("file") TempFile[] file, @Param("fileNames") String[] fileNames,
//							  @Param(value = "first", df = "true") boolean first) throws IOException {
//		int fileNum = (int) Stream.of(file).filter(f -> f.getSubmittedFileName().toLowerCase().endsWith(".jar")).count();
//
//		if (fileNum <= 0) {
//			LOG.warn(" not find any jar file!");
//		}
//
//		JarService jarService = JarService.getOrCreate(groupName) ;
//		try {
//			if(!first){
//				for (int i = 0; i < fileNames.length; i++) {
//					String fileName = fileNames[i];
//					if (fileName.toLowerCase().endsWith(".jar")) {
//						try {
//							File to = new File(jarService.getJarPath()+"/" + fileName);
//							file[i].write(to.getAbsolutePath());
//							LOG.info("write file to " + to.getAbsolutePath());
//							fileNum++;
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					} else {
//						LOG.warn(fileName + " not a jar ! so skip it!");
//					}
//				}
//				JarService.remove(groupName);
//			}else{
//				Set<String> hostPortsArr = new HashSet<>();
//				Arrays.stream(hostPorts).forEach(s -> hostPortsArr.add((String) s));
//				File[] files = Arrays.stream(file).map(f -> f.getFile()).toArray(File[]::new) ;
//				String[] fns = Arrays.stream(file).map(f -> f.getSubmittedFileName()).toArray(String[]::new) ;
//				proxyService.upload(hostPortsArr, "/admin/jar/uploadJar",ImmutableMap.of("group_name",groupName,"file",files,
//						"fileNames",fns,"first",false) , 100000);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return Restful.instance().ok(false).msg("保存失败！" + e.getMessage());
//		}
//		return Restful.instance().ok(true).msg("upload " + fileNum + " file ok!");
//	}
//}
//
//
//
