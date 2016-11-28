//package cn.com.infcn.api.test;
//
//import java.util.Date;
//
//import org.nlpcn.jcoder.filter.TokenFilter;
//import org.nlpcn.jcoder.run.annotation.Cache;
//import org.nlpcn.jcoder.run.annotation.Execute;
//import org.nlpcn.jcoder.util.DateUtils;
//import org.nlpcn.jcoder.util.Restful;
//import org.nlpcn.jcoder.util.Testing;
//import org.nutz.ioc.loader.annotation.Inject;
//import org.nutz.mvc.annotation.By;
//import org.nutz.mvc.annotation.Filters;
//import org.nutz.mvc.annotation.Param;
//import org.slf4j.Logger;
//
///**
// * 测试api
// * 
// * @author Ansj
// *
// */
//public class TestApi {
//	
//	public static void main(String[] args) throws Exception {
//		Restful test = Testing.instance(TestApi.class,"/Users/sunjian/Documents/workspace/jcoder_sdk_search/src/test/resources/ioc.js").test("ansj", new Date()) ;
//		
//		System.out.println(test.getObj());
//	}
//	
//
//	@Inject
//	private Logger log;
//
//	int k = 0  ;
//	/**
//	 * 测试创建api
//	 * 
//	 * @param name 姓名
//	 * @param aaa 日期
//	 * @return 拼接好的字符串
//	 */
//	@Execute
//	@Cache(size = 100, time = 20)
//	@Filters(@By(type=.class) )
//	public Restful test(String name, Date aaa) {
//		for (int i = 0; i < 100; i++) {
//			log.info("" + i);
//		}
//
//		aaa.getTime();
//
//		return Restful.instance("ok " + name + " " + DateUtils.formatDate(aaa, DateUtils.SDF_FORMAT)+"   num: "+(k++));
//	}
//
//}
