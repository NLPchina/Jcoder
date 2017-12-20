package org.nlpcn.jcoder.controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;

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
public class JarAction {

	private static final Logger LOG = LoggerFactory.getLogger(JarAction.class) ;

	@Inject
	private TaskService taskService;

	@At("/jar/list")
	@Ok("jsp:/jar_list.jsp")
	public Object list(@Param("group_name") String groupName) throws IOException, URISyntaxException {

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

		return result;
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

	@At("/jar/maven")
	@Ok("jsp:/maven.jsp")
	public Restful show(@Param("group_name") String groupName) {
		JarService jarService = JarService.getOrCreate(groupName) ;

		JSONObject job = new JSONObject();
		job.put("content", IOUtil.getContent(new File(jarService.getPomPath()), IOUtil.UTF8));
		job.put("mavenPath", jarService.getMavenPath());
		return Restful.instance(job);

	}

	@At("/maven/save")
	@Ok("json")
	public Restful save(@Param("group_name") String groupName ,@Param("maven_path") String mavenPath, @Param("content") String content) throws IOException, NoSuchAlgorithmException {
		JarService jarService = JarService.getOrCreate(groupName) ;
		String savePom = jarService.savePom(mavenPath,content);
		return Restful.instance().msg(savePom.replace("\n", "</br>"));
	}

	@At("/jar/upload")
	@Ok("raw")
	@AdaptBy(type = UploadAdaptor.class)
	public Restful uploadJar(@Param("group_name") String groupName, @Param("file") TempFile[] file) throws IOException {

		int fileNum = (int) Stream.of(file).filter(f -> f.getSubmittedFileName().toLowerCase().endsWith(".jar")).count();

		if (fileNum <= 0) {
			LOG.warn(" not find any jar file!");
		}

		JarService jarService = JarService.getOrCreate(groupName) ;

		synchronized (jarService) {

			for (TempFile tempFile : file) {
				String fileName = tempFile.getSubmittedFileName();
				if (fileName.toLowerCase().endsWith(".jar")) {
					try {
						File to = new File(jarService.getJarPath()+"/" + tempFile.getSubmittedFileName());
						tempFile.write(to.getAbsolutePath());
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
			return Restful.instance().msg("upload " + fileNum + " file ok!");
		}
	}

}
