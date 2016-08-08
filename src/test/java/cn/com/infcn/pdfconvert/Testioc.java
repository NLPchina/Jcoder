package cn.com.infcn.pdfconvert;

import java.io.File;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.server.rpc.client.VFile;
import org.nlpcn.jcoder.server.rpc.server.Rpcs;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;

public class Testioc {

	static {
		System.out.println("----------compilaaae----------------");
	}

	@Inject
	private Logger log;

	@Execute
	public Object searchData(VFile file) throws Exception {
		log.info(file);
		log.info(Rpcs.getRep());
		log.info(Rpcs.getReq());
		
		file.writeToFile(new File("D:/aaa"), "aaa.dic");
		
		return StaticValue.OK;
	}

}
