package org.nlpcn.jcoder.util;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.CodeException;
import org.nlpcn.jcoder.run.java.JavaSourceUtil;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 监听文件和文件夹变化
 */
public class GroupFileListener extends FileAlterationListenerAdaptor {

	private static final Logger LOG = LoggerFactory.getLogger(GroupFileListener.class);

	private static final ConcurrentHashMap<String, Object[]> MAP = new ConcurrentHashMap<>();

	/**
	 * 记录taskname对应的file文件路径
	 */
	private ConcurrentHashMap<String, File> taskFileMap = new ConcurrentHashMap<>();
	private String groupName;
	private File srcFile;
	private File pomFile;
	private File iocFile;

	public GroupFileListener(String groupName) {
		this.groupName = groupName;
		srcFile = new File(StaticValue.GROUP_FILE, groupName + "/src/api");
		pomFile = new File(StaticValue.GROUP_FILE, groupName + "/pom.xml");
		iocFile = new File(StaticValue.GROUP_FILE, groupName + "/resources/ioc.js");
	}

	/**
	 * 注册一个监听事件
	 */
	public static void regediter(String groupName) {
		GroupFileListener groupFileListener = new GroupFileListener(groupName);
		groupFileListener.init();
		FileAlterationMonitor src = createMonitor(groupFileListener.srcFile, groupFileListener);

		FileListener ioc = new FileListener(groupFileListener.iocFile, (v) -> {
			JarService.getOrCreate(groupName).release();
			return null;
		});
		ioc.start();

		FileListener pom = new FileListener(groupFileListener.pomFile, (v) -> {
			JarService.getOrCreate(groupName).release();
			JarService.getOrCreate(groupName);
			return null;
		});
		pom.start();

		MAP.put(groupName, new Object[]{src, pom, ioc});
	}

	/**
	 * 创建监控
	 *
	 * @param file
	 * @param groupFileListener
	 * @return
	 */
	private static FileAlterationMonitor createMonitor(File file, GroupFileListener groupFileListener) {
		FileAlterationObserver observer = new FileAlterationObserver(file, null, null);
		observer.addListener(groupFileListener);
		FileAlterationMonitor monitor = new FileAlterationMonitor(100, observer);
		try {
			monitor.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return monitor;
	}

	public static void writeTask2Src(Task t) throws IOException, CodeException {
		LOG.info("syn write file by task " + t.getName());
		String pk = new JavaSourceUtil(t.getCode()).getPackage();
		File file = new File(StaticValue.GROUP_FILE, t.getGroupName() + "/src/api/" + pk.replace(".", "/") + "/" + t.getName() + ".java");

		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();

		}
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(t.getCode().getBytes("utf-8"));
		}
	}

	/**
	 * 注销一个监听事件
	 */
	public static void unRegediter(String groupName) {
		Object[] remove = MAP.remove(groupName);
		if (remove != null) {
			for (Object o : remove) {
				if (o instanceof FileAlterationMonitor) {
					try {
						((FileAlterationMonitor) o).stop();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (o instanceof FileListener) {
					try {
						((Thread) o).interrupt();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	/**
	 * 初始化做一次同步
	 */
	private void init() {
		try {

			List<File> allApi = new ArrayList<>();

			File srcFile = new File(StaticValue.GROUP_FILE, groupName + "/src/api");

			if (!srcFile.exists()) {
				srcFile.mkdirs();
			}

			File srcMainFile = new File(StaticValue.GROUP_FILE, groupName + "/src/main");
			if (!srcMainFile.exists()) {
				srcMainFile.mkdirs();
			}

			File srcTestFile = new File(StaticValue.GROUP_FILE, groupName + "/src/test");
			if (!srcTestFile.exists()) {
				srcTestFile.mkdirs();
			}

			Files.walkFileTree(srcFile.toPath(), new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toFile().getName().endsWith(".java")) {
						allApi.add(file.toFile());
					}
					return FileVisitResult.CONTINUE;
				}
			});

			Map<String, Task> maps = new HashMap<>();

			StaticValue.getSystemIoc().get(TaskService.class, "taskService").findTasksByGroupName(groupName).forEach(t -> {
				try {
					maps.put(t.getName(), t);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("init err {}", t.getCode());
				}
			});

			allApi.stream().collect(Collectors.groupingBy(f -> fileName(f))).forEach((name, files) -> {

				if (files.size() > 1) {
					printLog("taskName:" + name + " name has more than one files :" + files);
					Task task = getTask(name);
					if (task != null) {
						for (File file : files) {
							String content = IOUtil.getContent(file, "utf-8");
							if (taskFileMap.get(name) == null || content.equals(task.getCode())) {
								LOG.warn("syn name {}", file.getPath());
								taskFileMap.put(name, file);
							}
						}
					} else {
						for (File file : files) {
							LOG.warn("not syn task for path " + file.getPath());
						}
					}
				} else {
					taskFileMap.put(name, files.get(0));
				}
			});


			allApi.stream().forEach(file -> {
				String taskName = fileName(file);
				Task task = getTask(taskName);
				if (task != null) {
					maps.remove(task.getName());
					this.onFileChange(file);
				} else {
					this.onFileCreate(file);
				}
			});

			maps.values().forEach(t -> {
				try {
					writeTask2Src(t);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onFileCreate(File file) {
		if (file.getName().endsWith(".java")) {
			try {
				createTask(file);
			} catch (CodeException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void createTask(File file) throws CodeException {

		LOG.info("[新建]:" + file.getAbsolutePath());


		String content = IOUtil.getContent(file, "utf-8");

		JavaSourceUtil javaSourceUtil = new JavaSourceUtil(content);

		String className = javaSourceUtil.getClassName();

		String fileName = fileName(file);

		File syn = taskFileMap.get(file);

		if (syn != null && !syn.equals(file) && syn.exists()) {
			printLog(String.format("file:%s can not syn ,because %s all ready exists", file.getPath(), syn.getPath()));
			return;
		}

		if (!fileName.equals(className)) {
			printLog(String.format("path:%s className:%s not equals fileName:%s...................................................................", file.getAbsoluteFile(), className, fileName));
			return;
		}

		Task task = getTask(className);

		if (task != null) {
			this.onFileChange(file);
		} else {
			task = new Task();
			task.setCode(content);
			task.setCreateUser("admin");
			task.setType(1);
			task.setStatus(1);
			task.setCreateTime(new Date());
			task.setUpdateTime(new Date());
			task.setGroupName(groupName);
			task.setDescription("file create");
			try {
				StaticValue.getSystemIoc().get(TaskService.class, "taskService").saveOrUpdate(task);
				flush(task.getName());
				taskFileMap.put(fileName, file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onFileChange(File file) {
		if (file.getName().endsWith(".java")) {

			String fileName = fileName(file);

			File synFile = taskFileMap.get(fileName);

			if (synFile != null && !file.equals(synFile) && synFile.exists()) {
				printLog(String.format("path:%s has same file in path:%s...................................................................", file, taskFileMap.get(fileName)));
				return;
			}


			Task task = getTask(fileName);

			if (task == null) {
				onFileCreate(file);
				return;
			} else {
				try {
					try {
						String fCode = IOUtil.getContent(file, "utf-8");

						if (fCode.equals(task.getCode())) {
							return;
						}

						LOG.info("[修改]:" + file.getAbsolutePath());
						task.setCode(fCode);

						if (!fileName.equals(task.getName())) {
							printLog(String.format("path:%s className:%s not equals fileName:%s...................................................................", file.getAbsoluteFile(), task.getName(), fileName));
							return;
						}

						StaticValue.getSystemIoc().get(TaskService.class, "taskService").saveOrUpdate(task);
						flush(task.getName());
						taskFileMap.put(fileName, file);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * 刷新這個類
	 *
	 * @param taskName
	 * @throws Exception
	 */
	private void flush(String taskName) {
		Set<String> taskNames = new HashSet<>(1);
		taskNames.add(taskName);
		try {
			StaticValue.space().different(groupName, taskNames, null, false, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printLog(String message) {
		LOG.error("...............................................................................................................................");
		LOG.error("...............................................................................................................................");
		LOG.error("...............................................................................................................................");
		LOG.error("...............................................................................................................................");
		LOG.error("...............................................................................................................................");
		LOG.error(message);
		LOG.error("...............................................................................................................................");
		LOG.error("...............................................................................................................................");
		LOG.error("...............................................................................................................................");
		LOG.error("...............................................................................................................................");
		LOG.error("...............................................................................................................................");
	}


	@Override
	public void onFileDelete(File file) {
		if (file.getName().endsWith(".java")) {
			System.out.println("[删除]:" + file.getAbsolutePath());

			String fileName = fileName(file);

			File f = taskFileMap.get(fileName);
			if (f != null && !f.equals(file) && f.exists()) {
				printLog(String.format("delete file:%s not equals %s skip syn", file.getPath(), f.getPath()));
				return;
			}


			Task task = getTask(fileName);
			if (task != null) {
				try {
					StaticValue.getSystemIoc().get(TaskService.class, "taskService").delete(task);
					StaticValue.getSystemIoc().get(TaskService.class, "taskService").delByDB(task);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			taskFileMap.remove(fileName);

			flush(fileName);
		}
	}


	private String taskName(File file) throws CodeException {
		String code = IOUtil.getContent(file, "utf-8");
		JavaSourceUtil sourceUtil = new JavaSourceUtil(code);
		return sourceUtil.getClassName();
	}

	private String fileName(File file) {
		return file.getName().substring(0, file.getName().length() - 5);
	}

	private Task getTask(String taskName) {
		Task task = StaticValue.getSystemIoc().get(TaskService.class, "taskService").findTask(groupName, taskName);
		return task;
	}

	private static class FileListener extends Thread {

		boolean flag = true;
		private long preStatus = 0;
		private File file = null;
		private Function<Void, Void> callBack;

		public FileListener(File file, Function<Void, Void> callBack) {
			if (file.isDirectory()) {
				throw new RuntimeException("it only listener file");
			}
			this.file = file;
			this.callBack = callBack;
			if (file.exists()) {
				preStatus = getStatus();
			}
		}

		@Override
		public void run() {
			while (flag) {
				if (preStatus != getStatus()) {
					preStatus = getStatus();
					callBack.apply(null);
				}
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					return;
				}
			}
		}

		@Override
		public void interrupt() {
			flag = false;
			super.interrupt();
		}

		public long getStatus() {
			if (file.exists()) {
				return file.lastModified();
			} else {
				return 0;
			}
		}
	}
}
