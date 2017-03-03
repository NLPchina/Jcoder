package cn.com.infcn.api.test;

import java.io.IOException;
import java.util.List;

import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.entity.Record;
import org.nutz.ioc.loader.annotation.Inject;
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
	
	@Inject
	private BasicDao mssDao ;

	/**
	 * 测试创建api
	 * 
	 * @param name 姓名
	 * @param aaa 日期
	 * @return 
	 * @return 拼接好的字符串
	 * @throws IOException 
	 */
	@Execute
	public List<Record> test() throws IOException {
		return mssDao.select("select * from etl_worker");
	}

	
}
