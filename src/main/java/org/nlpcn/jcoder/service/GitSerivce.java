package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.nlpcn.jcoder.constant.Constants;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.domain.GroupGit;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.CodeException;
import org.nlpcn.jcoder.run.java.JavaSourceUtil;
import org.nlpcn.jcoder.util.*;
import org.nutz.http.Response;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
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
import java.util.stream.Collectors;

@IocBean
public class GitSerivce {
	private static final Logger LOG = LoggerFactory.getLogger(GitSerivce.class);

	@Inject
	private TaskService taskService;

	@Inject
	private ProxyService proxyService;

	/**
	 * 获得远程的全部分支
	 */
	public Collection<Ref> getRemoteBranchs(String uri, String userName, String password) throws GitAPIException {
		LsRemoteCommand lsRemoteCommand = Git.lsRemoteRepository().setRemote(uri).setTags(false).setHeads(true);

		if (StringUtil.isNotBlank(userName)) {
			lsRemoteCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password));
		}
		return lsRemoteCommand.call();
	}

	/**
	 * 從git刷新一個組
	 */
	public synchronized String flush(GroupGit groupGit) throws Exception {
		String groupName = groupGit.getGroupName();

		CredentialsProvider provider = null;
		if (StringUtil.isNotBlank(groupGit.getUserName())) {
			provider = new UsernamePasswordCredentialsProvider(groupGit.getUserName(), groupGit.getPassword());
		} else {
			provider = UsernamePasswordCredentialsProvider.getDefault();
		}

		if (StringUtil.isBlank(groupGit.getUri())) {
			return "未定义git仓库";
		}
		String branch = groupGit.getBranch();
		if (StringUtil.isBlank(branch)) {
			return "未定义branch";
		}

		boolean hasBranch = false;

		//查询lastversion是否一样
		Collection<Ref> remoteBranchs = getRemoteBranchs(groupGit.getUri(), groupGit.getUserName(), groupGit.getPassword());
		for (Ref ref : remoteBranchs) {
			String name = ref.getName().split("/")[2];
			if (branch.equals(name)) {
				if (ref.getObjectId().getName().equals(groupGit.getMd5())) {
					return "md5一致已经是最新版本";
				} else {
					groupGit.setMd5(ref.getObjectId().getName()); //先设置为最新的md5
					hasBranch = true;
				}
			}
		}

		if (!hasBranch) {
			return "没有找到分支: " + branch;
		}

		File groupToken = new File(StaticValue.HOME_FILE, "git/" + groupName + "_token");

		String token = "empty";

		if (groupToken.exists()) {
			token = IOUtil.getContent(groupToken, IOUtil.UTF8).trim();
		}

		boolean clone = false;
		//判断git目录是否存在。
		File groupDir = new File(StaticValue.HOME_FILE, "/git/" + groupName);
		if (!groupDir.exists() || !groupGit.getToken().equals(token)) {

			for (int i = 0; i < 10 && groupDir.exists(); i++) {
				org.nutz.lang.Files.deleteDir(groupDir);
				System.gc();
				Thread.sleep(1000);
				LOG.info("to delete dir {} times {}", groupDir.getPath(), i + 1);
			}
			if (groupDir.exists()) {
				return "删除目录" + groupDir.getPath() + "失败";
			}
			clone = true;
		} else {
			//TODO：判断.git 文件是否正确
		}

		Git git = null;

		try {

			if (clone) {
				LOG.info("to clone from: " + groupGit.getUri());
				CloneCommand command = Git.cloneRepository().setURI(groupGit.getUri()).setDirectory(groupDir).setBranch(branch);
				git = command.setCredentialsProvider(provider).call();
			} else {
				git = Git.open(groupDir);
			}

			PullResult call = git.pull().setCredentialsProvider(provider).call();//进行更新

			if (!call.isSuccessful()) {
				return call.toString();
			}


			LOG.info(call.getMergeResult().getMergeStatus().toString());

			List<String> relativePaths = new ArrayList<>();

			relativePaths.addAll(resourceSyn(groupName));

			//进行task同步
			relativePaths.addAll(apiSyn(groupGit, groupName, groupDir));

			String message = "文件无变动";

			//当前节点同步到主节点，和其他同步节点
			if (relativePaths.size() > 0) {
				Response post = proxyService.post(StaticValue.getHostPort(), "/admin/group/fixDiff",
						Maps.hash("fromHostPort", StaticValue.getHostPort(), "toHostPort", Constants.HOST_MASTER, "groupName", groupName, "relativePath[]", relativePaths.toArray()), 100000);
				message = Restful.instance(post).getMessage();

			}


			return message;


		} finally {
			try (FileOutputStream fos = new FileOutputStream(groupToken)) { //写入当前版本的token
				fos.write(groupGit.getToken().getBytes("utf-8"));
			}
			if (git != null) {
				git.close();
			}


			groupGit.setLastPullTime(new Date());
			StaticValue.space().setData2ZK(SharedSpaceService.GROUP_PATH + "/" + groupName, JSONObject.toJSONBytes(groupGit));
		}
	}

	/**
	 * 和git进行文件增量同步
	 */
	private List<String> resourceSyn(String groupName) throws IOException {

		//判断文件有无改动
		List<FileInfo> groupFileInfos = FileInfoService.listFileInfos(groupName, StaticValue.GROUP_FILE);
		List<FileInfo> gitFileInfos = FileInfoService.listFileInfos(groupName, new File(StaticValue.HOME_FILE, "git"));

		List<String> relativePaths = new ArrayList<>();

		Map<String, FileInfo> maps = new HashMap<>();
		groupFileInfos.stream().forEach(info -> maps.put(info.getRelativePath(), info));

		List<FileInfo> changeList = new ArrayList<>();

		for (FileInfo gitInfo : gitFileInfos) {
			if (gitInfo.isDirectory()) { //目录不同步
				continue;
			}
			FileInfo groupInfo = maps.get(gitInfo.getRelativePath());
			if (groupInfo == null) {
				changeList.add(gitInfo);
			} else if (!groupInfo.getMd5().equals(MD5Util.md5(gitInfo.file()))) {
				changeList.add(gitInfo);
			}
		}

		if (changeList.size() > 0) {
			try {
				JarService.lock(groupName);
				// 关闭classloader，和Ioc
				JarService.getOrCreate(groupName).release();
				System.gc();
				Thread.sleep(1000L);

				//进行文件同步
				for (FileInfo gitInfo : changeList) { //进行文件拷贝
					org.nutz.lang.Files.copy(gitInfo.file(), new File(StaticValue.GROUP_FILE + "/" + groupName + gitInfo.getRelativePath()));
					relativePaths.add(gitInfo.getRelativePath());
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				JarService.unLock(groupName);
			}
		}
		return relativePaths;
	}

	private List<String> apiSyn(GroupGit groupGit, String groupName, File groupDir) throws IOException {

		File srcFile = new File(groupDir, "src/api");

		List<File> allApi = new ArrayList<>();


		List<String> relativePaths = new ArrayList<>();

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

		taskService.findTaskByGroupNameCache(groupName).forEach(t -> {
			try {
				maps.put(t.getName(), t);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("init err {}", t.getCode());
			}
		});

		ConcurrentHashMap<String, File> taskFileMap = new ConcurrentHashMap<>();

		allApi.stream().collect(Collectors.groupingBy(f -> fileName(f))).forEach((name, files) -> {
			if (files.size() > 1) { //发生task重名
				throw new RuntimeException("taskName:" + name + " name has more than one files :" + files);
			} else {
				taskFileMap.put(name, files.get(0));
			}
		});

		//先验证
		allApi.stream().forEach(f -> {
			try {
				validate(f);
			} catch (CodeException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});


		//保存修改task
		allApi.stream().forEach(file -> {
			String taskName = fileName(file);
			Task task = getTask(groupName, taskName);
			if (task != null) {
				maps.remove(task.getName());
				changeTask(groupName, file, relativePaths);
			} else {
				relativePaths.add(fileName(file));
				createTask(groupGit.getGroupName(), file);
			}
		});

		//删除没有同步的class
		maps.values().forEach(t -> {
			deleteTask(t);
			relativePaths.add(t.getName());
		});

		return relativePaths;
	}

	private void deleteTask(Task task) {
		LOG.info("to del task {} in group {}", task.getName(), task.getGroupName());
		try {
			taskService.delete(task);
			taskService.delByDB(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void changeTask(String groupName, File file, List<String> relativePaths) {
		Task task = getTask(groupName, fileName(file));
		String fCode = IOUtil.getContent(file, "utf-8");
		if (fCode.equals(task.getCode())) {
			return;
		}
		LOG.info("[修改]:" + file.getAbsolutePath());
		task.setCode(fCode);
		task.setUpdateUser("git");
		try {
			taskService.saveOrUpdate(task);
			relativePaths.add(task.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}


	}


	private String fileName(File file) {
		return file.getName().substring(0, file.getName().length() - 5);
	}

	private Task getTask(String groupName, String taskName) {
		Task task = taskService.findTask(groupName, taskName);
		return task;
	}

	public void validate(File file) throws CodeException {
		LOG.info("[验证]:" + file.getAbsolutePath());


		String content = IOUtil.getContent(file, "utf-8");

		JavaSourceUtil javaSourceUtil = new JavaSourceUtil(content);

		String className = javaSourceUtil.getClassName();

		String fileName = fileName(file);

		if (!fileName.equals(className)) {
			throw new CodeException(String.format("path:%s className:%s not equals fileName:%s...................................................................", file.getAbsoluteFile(), className, fileName));
		}

	}


	private synchronized void createTask(String groupName, File file) {

		LOG.info("[新建]:" + file.getAbsolutePath() + " ");

		String content = IOUtil.getContent(file, "utf-8");

		Task task = new Task();
		task.setCode(content);
		task.setCreateUser("git");
		task.setType(1);
		task.setStatus(1);
		task.setCreateTime(new Date());
		task.setUpdateTime(new Date());
		task.setGroupName(groupName);
		task.setDescription("git create");
		try {
			taskService.saveOrUpdate(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
