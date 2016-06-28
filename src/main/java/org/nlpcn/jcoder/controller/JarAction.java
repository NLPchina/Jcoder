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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.jcoder.domain.JarInfo;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.run.java.DynamicEngine;
import org.nlpcn.jcoder.run.java.JavaSourceUtil;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.JsonResult;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;

import com.alibaba.fastjson.JSONObject;

@IocBean
@Filters(@By(type = AuthoritiesManager.class, args = { "userType", "1", "/login.jsp" }))
public class JarAction {

	private static final Logger LOG = Logger.getLogger(JarAction.class);

	@Inject
	private TaskService taskService;

	@At("/jar/list")
	@Ok("jsp:/jar_list.jsp")
	public Object list() throws IOException, URISyntaxException {

		List<File> findAllJar = JarService.findJars();

		Set<String> libPathSet = JarService.getLibPathSet();

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
			if (tempJarInfo.getIsMavenJar()) {
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

			if (tempJarInfo.getIsMavenJar()) {
				tempSet = result.get("Maven");
			} else {
				tempSet = result.get("File");
			}

			tempSet.add(tempJarInfo);
		});

		List<File> findSystemJars = JarService.findSystemJars();

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
	public void downDevSDK(HttpServletResponse response, @Param("resource") boolean resource) throws URISyntaxException, IOException {

		List<File> jars = new ArrayList<>();

		jars.addAll(JarService.findSystemJars());
		jars.addAll(JarService.findJars());

		Collection<Task> taskList = TaskService.findTaskList(null);

		byte[] buffer = new byte[10240];

		int len = 0;

		response.addHeader("Content-Disposition", "attachment;filename=jdcoder_sdk_" + DateUtils.formatDate(new Date(), DateUtils.SDF_TIMESTAP) + ".zip");
		response.setContentType("application/octet-stream");

		try (ZipOutputStream out = new ZipOutputStream(response.getOutputStream())) {

			// 写jar包
			for (File jar : jars) {
				if (jar.isDirectory() || !jar.canRead() || !jar.getName().toLowerCase().endsWith(".jar")) {
					continue;
				}
				out.putNextEntry(new ZipEntry("jcoder_sdk/lib/" + jar.getName()));
				try (FileInputStream fis = new FileInputStream(jar)) {
					while ((len = fis.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
				}
			}

			// 写task任务
			for (Task task : taskList) {
				try {
					authValidateView(task.getGroupId());
					String code = task.getCode();
					String path = JavaSourceUtil.findPackage(code);
					String className = JavaSourceUtil.findClassName(code);
					out.putNextEntry(new ZipEntry("jcoder_sdk/src/" + path.replace(".", "/") + "/" + className + ".java"));
					out.write(code.getBytes("utf-8"));
				} catch (RuntimeException e) {
				}
			}

			if (resource) { // is incloud resource dir
				File file = new File(StaticValue.HOME + "/resource");
				String basePath = new File(StaticValue.HOME).getAbsolutePath();
				Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path tempFile, BasicFileAttributes attrs) throws IOException {

						File f = tempFile.toFile();

						out.putNextEntry(new ZipEntry(f.getAbsolutePath().replace(basePath, "")));

						int len = 0;
						byte[] buffer = new byte[10240];
						if (!f.isDirectory() && f.canRead()) {
							try (FileInputStream fis = new FileInputStream(f)) {
								while ((len = fis.read(buffer)) > 0) {
									out.write(buffer, 0, len);
								}
							}
						}

						return FileVisitResult.CONTINUE;
					}
				});
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
	public Object remove(@Param("path") String path) throws IOException {
		if (JarService.removeJar(new File(path))) {
			return StaticValue.okMessage("成功删除jar:" + path);
		} else {
			return StaticValue.errMessage("失败删除jar:" + path + " 可能是因为这不是一个jar包,或者jar包是maven管理的");
		}
	}

	@At("/jar/maven")
	@Ok("jsp:/maven.jsp")
	public Object show() {
		JSONObject job = new JSONObject();
		job.put("content", IOUtil.getContent(new File(JarService.POM), IOUtil.UTF8));
		job.put("mavenPath", JarService.getMavenPath());
		return StaticValue.makeReuslt(true, job);

	}

	@At("/maven/save")
	@Ok("json")
	public JsonResult save(@Param("mavenPath") String mavenPath, @Param("content") String content) throws IOException, NoSuchAlgorithmException {
		JarService.setMavenPath(mavenPath);
		String savePom = JarService.savePom(content);
		return StaticValue.okMessageJson(savePom.replace("\n", "</br>"));
	}

	@At("/jar/upload")
	@Ok("raw")
	@AdaptBy(type = UploadAdaptor.class)
	public String uploadJar(@Param("file") TempFile[] file) throws IOException {

		int fileNum = (int) Stream.of(file).filter(f -> f.getSubmittedFileName().toLowerCase().endsWith(".jar")).count();

		if (fileNum <= 0) {
			LOG.warn(" not find any jar file!");
		}

		synchronized (DynamicEngine.getInstance()) {

			DynamicEngine.close();

			for (TempFile tempFile : file) {
				String fileName = tempFile.getSubmittedFileName();
				if (fileName.toLowerCase().endsWith(".jar")) {
					try {
						File to = new File(StaticValue.HOME + "/lib/" + tempFile.getSubmittedFileName());
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

			JarService.flushClassLoader();
			return StaticValue.okMessage("成功上传 " + fileNum + " 个文件!");
		}
	}
}
