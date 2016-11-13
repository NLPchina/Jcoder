package org.nlpcn.jcoder.filter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * this filter record err times by err() , if times gather than maxCount , it while be shield your request
 * 
 * @author ansj
 *
 */
public class IpErrorCountFilter implements ActionFilter {

	private static final Logger LOG = LoggerFactory.getLogger(IpErrorCountFilter.class);

	private static final Cache<String, AtomicInteger> IP_CACHW = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(20, TimeUnit.MINUTES).build();

	private int maxCount;

	public IpErrorCountFilter(String maxCountStr) {
		if (StringUtil.isBlank(maxCountStr)) {
			maxCount = 1;
		} else {
			this.maxCount = Integer.parseInt(maxCountStr);
		}
	}

	@Override
	public View match(ActionContext actionContext) {

		String ip = StaticValue.getRemoteHost(actionContext.getRequest());

		AtomicInteger times = IP_CACHW.getIfPresent(ip);
		
		if(times==null){
			return null;
		}

		if (times.get() >= maxCount) {
			LOG.info(ip + "err times gather than " + maxCount);
			return new JsonView(Restful.instance(false, "your err times gather than " + maxCount + " please wait 20 minute try again",null,ApiException.Unauthorized));
		}

		return null;
	}

	public static int err() {
		return err(StaticValue.getRemoteHost(Mvcs.getReq()));

	}

	public static int err(String key) {
		try {
			AtomicInteger count = IP_CACHW.get(key, () -> {
				return new AtomicInteger();
			});
			int err = count.incrementAndGet() ;
			LOG.info(key + " err times " + err);
			return err ;
		} catch (ExecutionException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(),e);
		}
		return 0 ;
	}

}
