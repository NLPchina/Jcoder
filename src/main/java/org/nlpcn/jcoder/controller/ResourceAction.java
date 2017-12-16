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
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
public class ResourceAction {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceAction.class) ;

	private static final File RESOURCE_ROOT = StaticValue.RESOURCE_FILE;

	@Inject
	private TaskService taskService;

	@At("/resource/list")
	@Ok("jsp:/resource_list.jsp")
	public Object list(@Param("path") String path) throws IOException, URISyntaxException {

		List<FileInfo> result = new ArrayList<>();

		File file = null;

		if (StringUtil.isBlank(path)) {
			file = RESOURCE_ROOT;
		} else {
			file = new File(path);
		}

		if (!file.getAbsolutePath().startsWith(RESOURCE_ROOT.getAbsolutePath())) {
			result.add(new FileInfo(RESOURCE_ROOT));
			return result;
		}

		if (!file.equals(RESOURCE_ROOT)) {
			result.add(new FileInfo(file.getParentFile()));
		}

		File[] files = file.listFiles();

		Stream.of(files).forEach(f -> result.add(new FileInfo(f)));

		return result;
	}

	@At("/resource/remove")
	@Ok("redirect:/resource/list")
	public Object remove(@Param("path") String path) throws IOException {

		File file = new File(path);

		if (file.equals(RESOURCE_ROOT) || !file.getAbsolutePath().startsWith(RESOURCE_ROOT.getAbsolutePath())) {
			StaticValue.errMessage("can not remove this path!");
		}

		deleteDirOrFile(file);

		if (file.exists()) {
			StaticValue.errMessage(file + " not removed , maybe it locked!");
		}

		return StaticValue.okMessage(file.getParent());

	}

	/**
	 * 删除空目录
	 * 
	 * @param dir
	 *            将要删除的目录路径
	 */
	private static void doDelete(File file) {
		boolean success = (file).delete();
		if (success) {
			LOG.info("Successfully deleted empty directory: " + file);
		} else {
			LOG.info("Failed to delete empty directory: " + file);
		}
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * 
	 * @param file
	 *            将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	private void deleteDirOrFile(File file) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (int i = 0; i < children.length; i++) {
				deleteDirOrFile(children[i]);
			}
		}
		doDelete(file);
		;
	}

	@At("/resource/down")
	@Ok("raw")
	public Object down(@Param("path") String path, HttpServletResponse response) throws IOException {
		File file = new File(path);

		if (!file.getAbsolutePath().startsWith(RESOURCE_ROOT.getAbsolutePath())) {
			return StaticValue.errMessage("can not down this path!");
		}

		response.setContentType("application/octet-stream");

		if (file.isDirectory()) {
			response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "utf-8") + ".zip");
			try (ZipOutputStream out = new ZipOutputStream(response.getOutputStream())) {
				Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path tempFile, BasicFileAttributes attrs) throws IOException {

						File f = tempFile.toFile();

						out.putNextEntry(new ZipEntry(f.getAbsolutePath().replace(file.getAbsolutePath(), "")));

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
		} else {
			response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "utf-8"));
			try (FileInputStream fis = new FileInputStream(file)) {
				try (OutputStream out = response.getOutputStream()) {
					int len = 0;
					byte[] buffer = new byte[10240];
					while ((len = fis.read(buffer)) > 0) {
						response.getOutputStream().write(buffer, 0, len);
					}
				}
			}
		}

		return null;

	}

	@At("/resource/upload")
	@Ok("raw")
	@AdaptBy(type = UploadAdaptor.class)
	public String uploadJar(@Param("path") String path, @Param("file") TempFile[] fileList) throws IOException {
		File file = null;

		if (StringUtil.isBlank(path)) {
			file = RESOURCE_ROOT;
		} else {
			file = new File(path);
		}

		if (!file.getAbsolutePath().startsWith(RESOURCE_ROOT.getAbsolutePath())) {
			return StaticValue.errMessage("can not upload file by this path!");
		}

		for (TempFile tempFile : fileList) {
			try {
				File to = new File(file, tempFile.getSubmittedFileName());
				tempFile.write(to.getAbsolutePath());
				LOG.info("write file to " + to.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return StaticValue.okMessage(path);
	}

	@At("/resource/crate_folder")
	@Ok("redirect:/resource/list")
	public void crateFolder(@Param("path") String path, @Param("floder") String floder) {
		File file = null;
		if (StringUtil.isBlank(path)) {
			file = RESOURCE_ROOT;
		} else {
			file = new File(path);
		}
		file = new File(file, floder);
		file.mkdirs();
	}
}
