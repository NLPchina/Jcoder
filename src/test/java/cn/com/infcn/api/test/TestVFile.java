package cn.com.infcn.api.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.server.rpc.client.RpcInputStream;
import org.nlpcn.jcoder.server.rpc.client.VFile;
import org.nutz.ioc.loader.annotation.Inject;

/**
 * 文件传输api接口
 * @author Ansj
 *
 */
public class TestVFile {

	static {
		System.out.println("----------compilaaae----------------");
	}

	@Inject
	private Logger log;

	@Execute
	public VFile vFileTest(VFile file) throws Exception {

		log.info(file);

		file.writeToFile(new File("/Users/sunjian/Desktop/"), "aaa.dic");

		log.info("writetofile");

		return new VFile(new FileInputStream(new File("/Users/sunjian/Desktop/aaa.dic")));

	}

	@Execute
	public int rpcStreamTest(VFile file) throws Exception {

		log.info(file);

		RpcInputStream inputStream = file.toInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

		int len = 0;

		String temp = null;

		while ((temp = br.readLine()) != null) {
			log.info(temp);
			len += temp.length();

		}

		return len;

	}

}
