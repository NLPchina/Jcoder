package cn.com.infcn.api.test;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.Mvcs;
import org.slf4j.Logger;

/**
 * 测试api
 * 
 * @author Ansj
 *
 */

public class TestApi {
	

	@Inject
	private Logger log;

	/**
	 * 测试创建api
	 * 
	 * @param name 姓名
	 * @param aaa 日期
	 * @return 拼接好的字符串
	 * @throws IOException 
	 */
	@Execute
	public void test() throws IOException {
		HttpServletRequest request = Mvcs.getReq() ;
		HttpServletResponse response = Mvcs.getResp() ;
		
		String characterEncode = request.getCharacterEncoding();
		String agent = request.getHeader("User-Agent");
        boolean isMSIE = (agent != null && agent.indexOf("Firefox") != -1);
     
        String filename = java.net.URLEncoder.encode("我爱北京天安门.doc");
        
        response.setCharacterEncoding(characterEncode);  
		response.setContentType("application/octet-stream");
		String exportType = "rdf";
		
        response.setHeader("Content-Disposition", "attachment;fileName="+filename+"."+exportType); 
        response.getOutputStream().write("aaaaaaaaaaaa".getBytes());
        response.getOutputStream().flush();
        
	}

	
}
