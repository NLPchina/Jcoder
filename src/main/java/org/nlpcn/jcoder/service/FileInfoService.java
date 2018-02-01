package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.domain.GroupCache;
import org.nlpcn.jcoder.util.IOUtil;
import org.nlpcn.jcoder.util.MD5Util;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 文件管理类
 */
@IocBean
public class FileInfoService {


	private static final Logger LOG = LoggerFactory.getLogger(FileInfoService.class);


	/**
	 * key: FileInfo
	 * value: md5(file), but pom ioc is content
	 */
	private static final LoadingCache<FileInfo, String> FILE_INFO_MD5_CACHE = CacheBuilder.newBuilder().maximumSize(10000).build(new CacheLoader<FileInfo, String>() {
		@Override
		public String load(FileInfo key) throws Exception {
			LOG.info("computer md5 for {}{}", key.getGroupName(), key.getRelativePath());
			return MD5Util.md5(key.file());
		}
	});

	/**
	 * 查询所有文件的fileInfo
	 *
	 * @param groupName
	 * @return
	 * @throws IOException
	 */
	public static List<FileInfo> listFileInfosByGroup(String groupName) throws IOException {

		final List<FileInfo> result = listFileInfos(groupName, StaticValue.GROUP_FILE);

		//先查缓存中是否存在用缓存做对比
		List<Long> collect = result.stream().map(fi -> fi.getLastModified()).sorted().collect(Collectors.toList());
		String nowTimeMd5 = MD5Util.md5(collect.toString()); //当前文件的修改时间md5

		GroupCache groupCache = getGroupCache(groupName);

		//本group本身的插入zk中用来比较md5加快对比
		FileInfo root = new FileInfo(new File(StaticValue.GROUP_FILE, groupName), false);
		root.setLength(result.stream().mapToLong(f -> f.getLength()).sum());

		if (groupCache != null && nowTimeMd5.equals(groupCache.getTimeMD5())) {
			LOG.info(groupName + " time md5 same so add it");
			root.setMd5(groupCache.getGroupMD5());
		} else {
			LOG.info("to computer md5 in gourp: " + groupName);
			List<String> ts = result.stream().map(fi -> fi.getRelativePath() + fi.getMd5()).sorted().collect(Collectors.toList());

			groupCache = new GroupCache();
			groupCache.setGroupMD5(MD5Util.md5(ts.toString()));
			groupCache.setTimeMD5(nowTimeMd5);
			groupCache.setPomMD5(JarService.getOrCreate(groupName).getPomMd5());
			root.setMd5(groupCache.getGroupMD5());

			IOUtil.Writer(new File(StaticValue.GROUP_FILE, groupName + ".cache").getCanonicalPath(), IOUtil.UTF8, JSONObject.toJSONString(groupCache));
		}


		result.add(root);

		return result;
	}

	/**
	 * 从一个root目录获取某个group的文件列表信息
	 *
	 * @param groupName
	 * @param root
	 * @return
	 * @throws IOException
	 */
	public static List<FileInfo> listFileInfos(String groupName, File root) throws IOException {
		boolean git = root.getName().equals("git");

		final List<FileInfo> result = new ArrayList<>();

		if (!new File(root, groupName).exists()) {
			LOG.warn(groupName + " not folder not exists so create it");
			new File(root, groupName).mkdirs();
		}

		Path[] paths = new Path[]{
				new File(root, groupName + "/resources").toPath(),
				new File(root, groupName + "/lib").toPath(),
		};


		File pom = new File(root, groupName + "/pom.xml");
		if (pom.exists()) {
			result.add(new FileInfo(pom, git));
		}

		for (Path path : paths) {
			if (!path.toFile().exists()) {
				LOG.warn("file {} not exists ", path);
				continue;
			}

			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				// 在访问子目录前触发该方法
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					File file = dir.toFile();
					if (!file.canRead() || file.isHidden() || file.getName().charAt(0) == '.') {
						LOG.warn(path.toString() + " is hidden or can not read or start whth '.' so skip it ");
						return FileVisitResult.SKIP_SUBTREE;
					}
					result.add(new FileInfo(file, git));
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					File file = path.toFile();
					if (!file.canRead() || file.isHidden() || file.getName().charAt(0) == '.') {
						LOG.warn(path.toString() + " is hidden or can not read or start whth '.' so skip it ");
						return FileVisitResult.CONTINUE;
					}
					try {
						result.add(new FileInfo(file, git));
					} catch (Exception e) {
						e.printStackTrace();
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
		return result;
	}

	/**
	 * 获取文件的缓存类
	 *
	 * @param groupName
	 * @return
	 */
	public static GroupCache getGroupCache(String groupName) {
		GroupCache groupCache = null;
		try {
			File cacheFile = new File(StaticValue.GROUP_FILE, groupName + ".cache");
			if (cacheFile.exists()) {
				String content = IOUtil.getContent(cacheFile, "utf-8");
				if (org.nlpcn.jcoder.util.StringUtil.isNotBlank(content)) {
					groupCache = JSONObject.parseObject(content, GroupCache.class);
				}
			}
		} catch (Exception e) {
			LOG.warn(groupName + " cache read err so create new ");
		}
		return groupCache;
	}

	public static String computerMD5(FileInfo fileInfo) {
		try {
			return FILE_INFO_MD5_CACHE.get(fileInfo);
		} catch (ExecutionException e) {
			e.printStackTrace();//不会发生理论上，如果发生就是bug
			return fileInfo.getName();
		}
	}

	public String getContent(String groupName, String relativePath, int maxSize) throws IOException {
		File file = new File(StaticValue.GROUP_FILE, groupName + relativePath);
		if (!file.exists()) {
			throw new FileNotFoundException("文件不存在");//obj是空
		}

		if (file.isDirectory()) {
			throw new FileNotFoundException(relativePath + " 是目录");
		}

		byte[] bytes = new byte[maxSize];

		try (FileInputStream fis = new FileInputStream(file)) {
			int len = fis.read(bytes);
			String content = "";
			if (len > 0) {
				content = new String(bytes, 0, len);
			}
			return content;
		}
	}
}
