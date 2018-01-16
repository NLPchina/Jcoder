package org.nlpcn.jcoder.util;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.h2.store.FileLister;
import org.nlpcn.jcoder.domain.ApiDoc;
import org.nlpcn.jcoder.domain.ClassDoc;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.run.java.JavaSourceUtil;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 监听文件和文件夹变化
 */
public class GroupFileListener extends FileAlterationListenerAdaptor {

	private static final Logger LOG = LoggerFactory.getLogger(GroupFileListener.class);

	private static final ConcurrentHashMap<String, Object[]> MAP = new ConcurrentHashMap<>();

	/**
	 * 注册一个监听事件
	 */
	public static void regediter(String groupName) {
		GroupFileListener groupFileListener = new GroupFileListener(groupName);
		groupFileListener.init();
		FileAlterationMonitor src = createMonitor(groupFileListener.srcFile, groupFileListener);

		FileListener ioc = new FileListener(groupFileListener.iocFile, (v) -> {
			JarService.getOrCreate(groupName).flushIOC();
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

			StaticValue.getSystemIoc().get(TaskService.class, "taskService").findTaskByGroupNameCache(groupName).forEach(t -> {
				maps.put(t.getName(), t);
			});


			allApi.stream().forEach(file -> {
				String taskName = taskName(file);
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
				} catch (IOException e) {
					e.printStackTrace();
				}

			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeTask2Src(Task t) throws IOException {
		LOG.info("syn write file by task " + t.getName());
		String pk = JavaSourceUtil.findPackage(t.getCode());
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

	private String groupName;

	private File srcFile;
	private File pomFile;
	private File iocFile;

	public GroupFileListener(String groupName) {
		this.groupName = groupName;
		JarService js = JarService.getOrCreate(groupName);
		srcFile = new File(StaticValue.GROUP_FILE, groupName + "/src/api");
		pomFile = new File(js.getPomPath());
		iocFile = new File(js.getIocPath());
	}

	@Override
	public void onFileCreate(File file) {
		if (file.getName().endsWith(".java")) {
			LOG.info("[新建]:" + file.getAbsolutePath());

			String content = IOUtil.getContent(file, "utf-8");

			try {

				List<ApiDoc> sub = null;
				if (!StringUtil.isBlank(content)) {
					ClassDoc parse = JavaDocUtil.parse(new StringReader(content));
					if (parse != null) {
						sub = parse.getSub();
					}
				}


				if (sub == null || sub.size() == 0) {
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error(file.getCanonicalPath() + "  not found any api  ...................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Task task = getTask(taskName(file));

			if (task != null) {
				if (task.getCode().equals(content)) {
					return;
				}
				try {
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error(task.getName() + "  已经存在你重名了........task name all ready in ...................................................................");
					LOG.error("==========" + JavaSourceUtil.findPackage(task.getCode()));
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
					LOG.error("...............................................................................................................................");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				task = new Task();
				task.setCode(IOUtil.getContent(file, "utf-8"));
				task.setCreateUser("admin");
				task.setType(1);
				task.setStatus(1);
				task.setCreateTime(new Date());
				task.setUpdateTime(new Date());
				task.setGroupName(groupName);
				task.setDescription("file create");
				try {
					StaticValue.getSystemIoc().get(TaskService.class, "taskService").saveOrUpdate(task);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onFileChange(File file) {
		if (file.getName().endsWith(".java")) {
			Task task = getTask(taskName(file));

			if (task == null) {
				onFileCreate(file);
				return;
			} else {

				String fCode = IOUtil.getContent(file, "utf-8");

				if (fCode.equals(task.getCode())) {
					return;
				}

				LOG.info("[修改]:" + file.getAbsolutePath());
				task.setCode(fCode);


				try {
					//try compile
					new JavaRunner(task).compile();
					try {
						StaticValue.getSystemIoc().get(TaskService.class, "taskService").saveOrUpdate(task);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}


	@Override
	public void onFileDelete(File file) {
		if (file.getName().endsWith(".java")) {
			System.out.println("[删除]:" + file.getAbsolutePath());

			Task task = getTask(taskName(file));

			if (task != null) {
				try {
					StaticValue.getSystemIoc().get(TaskService.class, "taskService").delete(task);
					StaticValue.getSystemIoc().get(TaskService.class, "taskService").delByDB(task);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}


	private String taskName(File file) {
		return file.getName().substring(0, file.getName().length() - 5);
	}


	private Task getTask(String taskName) {
		Task task = StaticValue.getSystemIoc().get(TaskService.class, "taskService").findTask(groupName, taskName);
		return task;
	}

	private static class FileListener extends Thread {

		private long preStatus = 0;

		private File file = null;

		boolean flag = true;

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
				return file.lastModified() ;
			} else {
				return 0;
			}
		}
	}
}
