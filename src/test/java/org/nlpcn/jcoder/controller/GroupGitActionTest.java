package org.nlpcn.jcoder.controller;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;
import org.nlpcn.jcoder.domain.GroupGit;
import org.nlpcn.jcoder.util.Restful;

import java.io.IOException;

import static org.junit.Assert.*;

public class GroupGitActionTest {

	@Test
	public void branchTest() throws IOException, GitAPIException {
		GroupGit groupGit = new GroupGit();
		groupGit.setUri("https://github.com/NLPchina/ansj_seg.git");
		Restful branch = new GroupGitAction().branch(groupGit);
	}

}