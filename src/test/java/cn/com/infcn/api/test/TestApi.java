package cn.com.infcn.api.test;

import java.io.IOException;
import java.util.Date;

import org.nlpcn.jcoder.filter.TokenFilter;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.Testing;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
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
	 */
	@Execute
	public Restful test(String name, Date aaa) {
		return Restful.instance("ok");
	}

	public static void main(String[] args) throws IOException {
		for (int i = 0; i < 1000000; i++) {
			try {
				Response response = Http.get("http://localhost:9085/IFCMonitorServlet") ;
				
				System.out.println(response.getContent());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
