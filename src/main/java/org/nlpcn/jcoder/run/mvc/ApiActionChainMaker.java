package org.nlpcn.jcoder.run.mvc;

import java.util.ArrayList;
import java.util.List;

import org.nlpcn.jcoder.run.mvc.processor.ApiAdaptorProcessor;
import org.nlpcn.jcoder.run.mvc.processor.ApiCrossOriginProcessor;
import org.nlpcn.jcoder.run.mvc.processor.ApiFailProcessor;
import org.nlpcn.jcoder.run.mvc.processor.ApiMethodInvokeProcessor;
import org.nlpcn.jcoder.run.mvc.processor.ApiModuleProcessor;
import org.nlpcn.jcoder.run.mvc.processor.ApiViewProcessor;
import org.nutz.mvc.ActionChainMaker;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Processor;
import org.nutz.mvc.impl.processor.ActionFiltersProcessor;
import org.nutz.mvc.impl.processor.EncodingProcessor;

public class ApiActionChainMaker implements ActionChainMaker {

	// 该接口只有一个方法
	public JcoderActionChain eval(NutConfig config, ActionInfo ai) {
		// 提醒: config可以获取ioc等信息, ai可以获取方法上的各种配置及方法本身
		// 正常处理的列表
		List<Processor> list = new ArrayList<>();
		list.add(init(config, ai, new EncodingProcessor())); // 设置编码信息@Encoding
		list.add(init(config, ai, new ApiCrossOriginProcessor())) ;//增加跨域支持
		list.add(init(config, ai, new ApiModuleProcessor())); // 获取入口类的对象,从ioc或直接new
		list.add(init(config, ai, new ActionFiltersProcessor())); // 处理@Filters
		list.add(init(config, ai, new ApiAdaptorProcessor())); // 处理@Adaptor
		ApiMethodInvokeProcessor apiMethodInvokeProcessor = new ApiMethodInvokeProcessor();
		list.add(init(config, ai, apiMethodInvokeProcessor)); // 执行入口方法
		list.add(init(config, ai, new ApiViewProcessor())); // 对入口方法进行渲染@Ok

		// 最后是专门负责兜底的异常处理器,这个处理器可以认为是全局异常处理器,对应@Fail
		Processor error = new ApiFailProcessor();
		try {
			error.init(config, ai);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return new JcoderActionChain(list, apiMethodInvokeProcessor, error, ai);
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
