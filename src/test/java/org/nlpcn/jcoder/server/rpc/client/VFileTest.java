package org.nlpcn.jcoder.server.rpc.client;

import java.io.File;

import org.junit.Test;

public class VFileTest {

	@Test
	public void test() {
		VFile file = new VFile(new File("README.md")) ;
		System.out.println(file.check());
	}

}
