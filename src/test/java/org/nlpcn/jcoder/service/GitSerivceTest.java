package org.nlpcn.jcoder.service;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.junit.Test;
import org.nutz.lang.Files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class GitSerivceTest {

	@Test
	public void getRemoteBranchsTest() throws GitAPIException, IOException {
		String uri = "https://github.com/NLPchina/Jcoder.git";
		String userName = null;
		String password = null;
		Collection<Ref> remoteBranchs = new GitSerivce().getRemoteBranchs(uri, userName, password);


		for (Ref remoteBranch : remoteBranchs) {
			System.out.println(remoteBranch.getObjectId().getName());
		}
	}


	@Test
	public void testClone() throws Exception {
		String uri = "http://192.168.10.158/LinYunLab/InfcnNlp.git";
		String userName = "ansj";
		String password = "****";
		String branchName = "master";
		boolean clone = !new File("test/.git").exists();

		File groupDir = new File("test");

		Git git = null;

		if (clone && groupDir.exists()) {
			for (int i = 0; i < 20 && groupDir.exists(); i++) {
				Files.deleteDir(groupDir);
				System.gc();
				Thread.sleep(100L);
			}

			if (groupDir.exists()) {
				throw new Exception("can not del dir : " + groupDir.getAbsolutePath());
			}
		}


		UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(userName, password);
		if (clone) {
			CloneCommand command = Git.cloneRepository().setURI(uri).setDirectory(groupDir).setBranch(branchName);
			git = command.setCredentialsProvider(provider).call();
		} else {
			git = Git.open(groupDir);
		}

		git.checkout().setAllPaths(true).call();

		ObjectId oldHead = git.getRepository().resolve("HEAD^{tree}");

		PullResult origin = git.pull().setRemote("origin").setRemoteBranchName(branchName).setCredentialsProvider(provider).call();//进行更新

		ObjectId head = git.getRepository().resolve("HEAD^{tree}");

		ObjectReader reader = git.getRepository().newObjectReader();
		CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
		oldTreeIter.reset(reader, oldHead);
		CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
		newTreeIter.reset(reader, head);
		List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();


		for (DiffEntry diff : diffs) {
			System.out.println(diff.getOldPath());
			System.out.println(diff.getNewPath());
		}


	}

}