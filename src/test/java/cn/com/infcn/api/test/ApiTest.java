package cn.com.infcn.api.test;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.nlpcn.jcoder.filter.TokenFilter;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试文档搜索
 * 
 * @author Ansj
 *
 */

public class ApiTest {

	@Inject
	private Logger log;

	/**
	 *
	 * @param i
	 * @return
	 * @throws Exception
	 */
	@DefaultExecute
	public Object test(int i, String content) throws Exception {
		Map<String,Object> hm = new HashMap<>() ;
		hm.put("name", "ansj") ;
		return TaskService.executeTask("TaskDemo", "execute", hm);
	}

}
