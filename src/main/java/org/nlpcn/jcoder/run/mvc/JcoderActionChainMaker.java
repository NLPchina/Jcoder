package org.nlpcn.jcoder.run.mvc;

import org.nlpcn.jcoder.run.mvc.processor.ApiFailProcessor;
import org.nlpcn.jcoder.run.mvc.processor.ApiViewProcessor;
import org.nutz.mvc.ActionChainMaker;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Processor;
import org.nutz.mvc.impl.NutActionChain;
import org.nutz.mvc.impl.processor.*;

import java.util.ArrayList;
import java.util.List;

/**
 * jcoder web的动作连
 */
public class JcoderActionChainMaker implements ActionChainMaker {

	// 该接口只有一个方法
	public NutActionChain eval(NutConfig config, ActionInfo ai) {
		// 提醒: config可以获取ioc等信息, ai可以获取方法上的各种配置及方法本身
		// 正常处理的列表

//		"default" : {
//			"ps" : [
//			"org.nutz.mvc.impl.processor.UpdateRequestAttributesProcessor",
//					"org.nutz.mvc.impl.processor.EncodingProcessor",
//					"org.nutz.mvc.impl.processor.ModuleProcessor",
//					"!org.nutz.integration.shiro.NutShiroProcessor",
//					"org.nutz.mvc.impl.processor.ActionFiltersProcessor",
//					"org.nutz.mvc.impl.processor.AdaptorProcessor",
//					"!org.nutz.plugins.validation.ValidationProcessor",
//					"org.nutz.mvc.impl.processor.MethodInvokeProcessor",
//					"org.nutz.mvc.impl.processor.ViewProcessor"
//		      ],
//			"error" : 'org.nutz.mvc.impl.processor.FailProcessor'
//		}

		List<Processor> list = new ArrayList<>();
		list.add(init(config, ai, new UpdateRequestAttributesProcessor())); // 设置代理类
		list.add(init(config, ai, new EncodingProcessor())); // 设置编码信息@Encoding
		list.add(init(config, ai, new ModuleProcessor()));//增加跨域支持
		list.add(init(config, ai, new ActionFiltersProcessor())); // 获取入口类的对象,从ioc或直接new
		list.add(init(config, ai, new AdaptorProcessor())); // 处理@Filters
		list.add(init(config, ai, new MethodInvokeProcessor())); // 处理@Adaptor
		list.add(init(config, ai, new ApiViewProcessor())); // 对入口方法进行渲染@Ok

		// 最后是专门负责兜底的异常处理器,这个处理器可以认为是全局异常处理器,对应@Fail
		Processor errorProcessor = new ApiFailProcessor();
		try {
			errorProcessor.init(config, ai);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return new NutActionChain(list, errorProcessor, ai);
	}

	public Processor init(NutConfig config, ActionInfo ai, Processor p) {
		try {
			p.init(config, ai);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return p;
	}
}
