package org.nlpcn.jcoder.run.java;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.Cache;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.run.annotation.Single;
import org.nlpcn.jcoder.util.Restful;
import org.nutz.ioc.loader.annotation.Inject;

/**
 * 测试文档搜索
 * @author Ansj
 *
 */
@Single(true)
public class ApiTest {@Inject private Logger log;

	@DefaultExecute @Cache
	public Object defaultTest(HttpServletRequest req,String name) throws InterruptedException {return Restful.instance("hello jcoder! ", null) ;}

}
