package org.nlpcn.jcoder.util;

public class CacheDomain {

	private long outTime;

	private Object obj;

	public CacheDomain(long outTime, Object obj) {
		this.outTime = outTime;
		this.obj = obj;
	}

	public boolean isOutTime() {
		if (outTime == 0) {
			return false;
		}
		return System.currentTimeMillis() > outTime;
	}

	@SuppressWarnings("unchecked")
	public <T> T getObject() {
		return (T) obj;
	}
}
