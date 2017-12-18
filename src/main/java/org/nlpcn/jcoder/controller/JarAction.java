package org.nlpcn.jcoder.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.util.*;
import org.nlpcn.jcoder.domain.JarInfo;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.run.java.JavaSourceUtil;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.TaskService;
import org.nutz.dao.Cnd;
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
	 * 下载开发者工具
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@At("/down/sdk")
	@Ok("raw")
	public void downDevSDK(@Param("group_name") String groupName ,HttpServletResponse response, @Param("resource") boolean resource) throws URISyntaxException, IOException {

		List<File> jars = new ArrayList<>();

		JarService jarService = JarService.getOrCreate(groupName) ;

		jars.addAll(jarService.findSystemJars());
		jars.addAll(jarService.findJars());

		Collection<Task> taskList = StaticValue.systemDao.search(Task.class, Cnd.where("status", "=", 1));

		byte[] buffer = new byte[10240];

		int len = 0;

		response.addHeader("Content-Disposition", "attachment;filename=jdcoder_sdk_" + DateUtils.formatDate(new Date(), DateUtils.SDF_TIMESTAP) + ".zip");
		response.setContentType("application/octet-stream");

		try (ZipOutputStream out = new ZipOutputStream(response.getOutputStream())) {

			Set<String> sets = new HashSet<>();
			// 写jar包
			for (File jar : jars) {
				if (jar.isDirectory() || !jar.canRead() || !jar.getName().toLowerCase().endsWith(".jar")) {
					continue;
				}

				// skip maven jar
				if (jar.getParentFile().getAbsolutePath().startsWith(new File(StaticValue.LIB_FILE,"target").getAbsolutePath())) {
					continue;
				}

				String name = "jcoder_sdk/lib/" + jar.getName();
				if (sets.contains(name)) {
					continue;
				}
				sets.add(name);
				out.putNextEntry(new ZipEntry("jcoder_sdk/lib/" + jar.getName()));
				try (FileInputStream fis = new FileInputStream(jar)) {
					while ((len = fis.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
				}
			}

			out.putNextEntry(new ZipEntry("jcoder_sdk/src/main/java/package-info.java"));
			out.write(("/**\n" + " * if you need make some jar file write in src package\n" + " */").getBytes());

			// 写task任务
			for (Task task : taskList) {
				try {
					authValidateView(task.getGroupId());
					String code = task.getCode();
					String path = JavaSourceUtil.findPackage(code);
					String className = JavaSourceUtil.findClassName(code);

					out.putNextEntry(new ZipEntry("jcoder_sdk/src/test/java/" + path.replace(".", "/") + "/" + className + ".java"));
					out.write(code.getBytes("utf-8"));
				} catch (RuntimeException e) {
				}
			}

			out.putNextEntry(new ZipEntry("jcoder_sdk/pom.xml"));
			out.write(IOUtil.getContent(new File(StaticValue.LIB_FILE, "pom.xml"), "utf-8")
					.replace("<artifactId>jcoder</artifactId>", "<artifactId>jcoder_" + UUID.randomUUID().toString() + "</artifactId>").getBytes());

			if (resource) { // is incloud resource dir
				File file = StaticValue.RESOURCE_FILE;
				String basePath = StaticValue.RESOURCE_FILE.getAbsolutePath();
				Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path tempFile, BasicFileAttributes attrs) throws IOException {

						File f = tempFile.toFile();

						if (f.isDirectory() || !f.canRead() || f.isHidden()) {
							return FileVisitResult.CONTINUE;
						}

						if (f.getParentFile().equals(StaticValue.RESOURCE_FILE) && "pom.xml".equals(f.getName())) { // skip pom.xml
							return FileVisitResult.CONTINUE;
						}

						String filePath = ("jcoder_sdk/src/test/resources/" + f.getAbsolutePath().replace(basePath, "")).replace("\\", "/").replace("//", "/");

						out.putNextEntry(new ZipEntry(filePath));

						int len = 0;
						byte[] buffer = new byte[10240];

						try (FileInputStream fis = new FileInputStream(f)) {
							while ((len = fis.read(buffer)) > 0) {
								out.write(buffer, 0, len);
							}
						}

						return FileVisitResult.CONTINUE;
					}
				});
			} else {
				out.putNextEntry(new ZipEntry("jcoder_sdk/src/test/resources/ioc.js"));
				out.write(IOUtil.getContent(new File(StaticValue.RESOURCE_FILE, "ioc.js"), "utf-8").getBytes());
			}
		}

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
			return StaticValue.okMessage("delete jar:" + path);
		} else {
			return StaticValue.errMessage("delete jar fail :" + path + " may be it is not a jar or it a maven jar");
		}
	}

	@At("/jar/maven")
	@Ok("jsp:/maven.jsp")
	public Object show(@Param("group_name") String groupName) {
		JarService jarService = JarService.getOrCreate(groupName) ;

		JSONObject job = new JSONObject();
		job.put("content", IOUtil.getContent(new File(jarService.getPomPath()), IOUtil.UTF8));
		job.put("mavenPath", jarService.getMavenPath());
		return StaticValue.makeReuslt(true, job);

	}

	@At("/maven/save")
	@Ok("json")
	public JsonResult save(@Param("group_name") String groupName ,@Param("maven_path") String mavenPath, @Param("content") String content) throws IOException, NoSuchAlgorithmException {
		JarService jarService = JarService.getOrCreate(groupName) ;
		String savePom = jarService.savePom(mavenPath,content);
		return StaticValue.okMessageJson(savePom.replace("\n", "</br>"));
	}

	@At("/jar/upload")
	@Ok("raw")
	@AdaptBy(type = UploadAdaptor.class)
	public String uploadJar(@Param("group_name") String groupName,@Param("file") TempFile[] file) throws IOException {

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
			return StaticValue.okMessage("upload " + fileNum + " file ok!");
		}
	}

}
