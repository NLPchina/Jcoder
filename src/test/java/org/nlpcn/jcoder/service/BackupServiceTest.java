package org.nlpcn.jcoder.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nlpcn.jcoder.server.H2Server;

public class BackupServiceTest {

	@Before
	public void init() {
		H2Server.startServer(null);
	}

	@Test
	public void test() {
	}

	@After
	public void shutDown() {
		H2Server.stopServer();
	}
}
