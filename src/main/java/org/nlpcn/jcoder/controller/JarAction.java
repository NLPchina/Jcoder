package org.nlpcn.jcoder.controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.util.*;
import org.nlpcn.jcoder.domain.JarInfo;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.TaskService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/jar")
@Ok("json")
@Fail("http:500")
public class JarAction {

	private static final Logger LOG = LoggerFactory.getLogger(JarAction.class) ;

	@Inject
	private TaskService taskService;

	@Inject
	private ProxyService proxyService;

	@At
	public Restful list(@Param("group_name") String groupName) throws IOException, URISyntaxException {

		JarService jarService = JarService.getOrCreate(groupName) ;

		List<File> findAllJar = jarService.findJars();

		Set<String> libPathSet = jarService.getLibPathSet();

		HashMap<String, TreeSet<JarInfo>> result = new HashMap<>();

		result.put("Maven", new TreeSet<>());
		result.put("File", new TreeSet<>());

		findAllJar.forEach(f -> {
			TreeSet<JarInfo> tempSet = null;

			JarInfo tempJarInfo = null;

			if (libPathSet.contains(f.getAbsolutePath())) {
				tempJarInfo = new JarInfo(f, 0);

			} else {
				tempJarInfo = new JarInfo(f, 1);
			}
			if (tempJarInfo.getIsMavenJar(groupName)) {
				tempSet = result.get("Maven");
			} else {
				tempSet = result.get("File");
			}

			tempSet.add(tempJarInfo);

			libPathSet.remove(f.getAbsolutePath());
		});

		libPathSet.forEach(path -> {
			TreeSet<JarInfo> tempSet = null;

			JarInfo tempJarInfo = null;

			tempJarInfo = new JarInfo(new File(path), 2);

			if (tempJarInfo.getIsMavenJar(groupName)) {
				tempSet = result.get("Maven");
			} else {
				tempSet = result.get("File");
			}

			tempSet.add(tempJarInfo);
		});

		List<File> findSystemJars = jarService.findSystemJars();

		TreeSet<JarInfo> treeSet = new TreeSet<>();

		for (File file : findSystemJars) {
			treeSet.add(new JarInfo(file, 0));
		}

		result.put("System", treeSet);

		return Restful.instance().ok(true).obj(result);
	}


	/**
	 * 查看权限验证
	 * 
	 * @param groupId
	 */
	private void authValidateView(Long groupId) {
		HttpSession session = Mvcs.getHttpSession();

		if ((Integer) session.getAttribute("userType") == 1) {
			return;
		}

		@SuppressWarnings("unchecked")
		Map<Long, Integer> authMap = (Map<Long, Integer>) session.getAttribute("AUTH_MAP");

		if (authMap.containsKey(groupId)) {
			return;
		}

		throw new RuntimeException("auth error !");
	}

	@At("/jar/remove")
	@Ok("redirect:/jar/list")
	public Object remove(@Param("group_name") String groupName,@Param("path") String path) throws IOException {
		if (JarService.getOrCreate(groupName).removeJar(new File(path))) {
			return Restful.instance().msg("delete jar:" + path);
		} else {
			return Restful.instance().msg("delete jar fail :" + path + " may be it is not a jar or it a maven jar");
		}
	}

	@At
	public Restful findMavenInfoByGroupName(@Param("group_name") String groupName) {
		try {
			JarService jarService = JarService.getOrCreate(groupName) ;
			return Restful.ok().obj(jarService.getPomInfo(groupName));
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.fail().msg(e.getMessage());
		}
	}

	@At
	public Restful save(@Param("hostPorts") String[] hostPorts,@Param("groupName") String groupName, @Param("content") String content,
						@Param(value = "first", df = "true") boolean first) throws IOException, NoSuchAlgorithmException {
		JarService jarService = JarService.getOrCreate(groupName) ;
		try {
			if(!first){
				jarService.savePom(groupName, content);
				//jarService.release();
				return Restful.instance().ok(true).msg("保存成功！");

			}else{
                Set<String> hostPortsArr = new HashSet<>();
                Arrays.stream(hostPorts).forEach(s -> hostPortsArr.add((String) s));
				String message = proxyService.post(hostPortsArr, "/admin/jar/save",
						ImmutableMap.of("groupName", groupName,"content", content,"first", false), 100000,
						ProxyService.MERGE_MESSAGE_CALLBACK);
				jarService.savePomInfo(groupName,content);
				return Restful.instance().ok(true).msg(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.instance().ok(false).msg("保存失败！" + e.getMessage());
		}
	}

	@At
	@AdaptBy(type = UploadAdaptor.class)
	public Restful uploadJar(@Param("hostPorts") String[] hostPorts,@Param("group_name") String groupName,
                             @Param("file") TempFile[] file,@Param("fileNames") String[] fileNames,@Param(value = "first", df = "true") boolean first) throws IOException {
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
