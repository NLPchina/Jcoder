package org.nlpcn.jcoder.run.mvc.processor;

import java.util.ArrayList;
import java.util.List;

import org.nlpcn.jcoder.filter.TokenFilter;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.ObjectInfo;
import org.nutz.mvc.Processor;
import org.nutz.mvc.View;
import org.nutz.mvc.impl.processor.AbstractProcessor;

/**
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author ansj
 *
 */
public class ApiActionFiltersProcessor extends AbstractProcessor {

	private static final Log log = Logs.get();

	protected List<ActionFilter> filters = new ArrayList<ActionFilter>();

	protected Processor proxyProcessor;

	protected Processor lastProcessor;

	public void init(NutConfig config, ActionInfo ai) throws Throwable {
		ObjectInfo<? extends ActionFilter>[] filterInfos = ai.getFilterInfos();
		if (null != filterInfos) {
			for (int i = 0; i < filterInfos.length; i++) {
				ActionFilter filter = evalObj(config, filterInfos[i]);
				filters.add(filter);
				if (filter instanceof Processor) {
					Processor processor = (Processor) filter;
					if (proxyProcessor == null) {
						proxyProcessor = processor;
						lastProcessor = processor;
					} else {
						processor.setNext(proxyProcessor);
						proxyProcessor = processor;
					}
				}
			}
		}
	}

	public void process(ActionContext ac) throws Throwable {

		if (ac.getRequest().getParameter("_rpc_init") == null) { //如果是null说明是调用激活此时放过
			for (ActionFilter filter : filters) {
				View view = filter.match(ac);
				if (null != view) {
					ac.setMethodReturn(view);
					renderView(ac);
					return;
				}
			}
		} else {
			log.info("to instance by init " + ac.getPath());
		}

		if (proxyProcessor == null) {
			doNext(ac);
		} else {
			if (lastProcessor != null)
				lastProcessor.setNext(next);
			proxyProcessor.process(ac);
		}
	}
}
