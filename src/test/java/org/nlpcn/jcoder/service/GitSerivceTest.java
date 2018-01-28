package org.nlpcn.jcoder.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;

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

}