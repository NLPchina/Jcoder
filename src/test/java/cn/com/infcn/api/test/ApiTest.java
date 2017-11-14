package cn.com.infcn.api.test;

import java.util.HashMap;
import java.util.Map;

import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.service.TaskService;

/**
 * 测试文档搜索
 * 
 * @author Ansj
 *
 */

public class ApiTest {


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
		hm.put("age", i) ;
		hm.put("sex", content) ;
		return TaskService.executeTask("TaskDemo", "execute", hm);
	}

}
