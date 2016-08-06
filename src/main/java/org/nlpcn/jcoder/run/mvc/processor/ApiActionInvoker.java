package org.nlpcn.jcoder.run.mvc.processor;

import org.nlpcn.jcoder.run.mvc.JcoderActionChain;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionChain;
import org.nutz.mvc.ActionContext;

public class ApiActionInvoker {

	private static final Log log = Logs.get();

	private ActionChain defaultChain;

	public ApiActionInvoker() {
	}

	public void setDefaultChain(ActionChain defaultChain) {
		this.defaultChain = defaultChain;
	}

	/**
	 * 根据动作链上下文对象，调用一个相应的动作链
	 * 
	 * @param ac
	 *            动作链上下文
	 * @return true- 成功的找到一个动作链并执行。 false- 没有找到动作链
	 */
	public boolean invoke(ActionContext ac) {
		ActionChain chain = getActionChain(ac);
		if (chain == null) {
			if (log.isDebugEnabled())
				log.debugf("Not chain for req (path=%s, method=%s)", ac.getPath(), ac.getRequest().getMethod());
			return false;
		}
		chain.doChain(ac);
		return ac.getBoolean(ActionContext.AC_DONE, true);
	}

	public ActionChain getActionChain(ActionContext ac) {
		return defaultChain;
	}
	
	public JcoderActionChain getChain(){
		return (JcoderActionChain) defaultChain ;
	}
}
