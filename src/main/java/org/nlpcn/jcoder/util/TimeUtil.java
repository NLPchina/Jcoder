/**
 * @Copyright 2015
 *
 **/
package org.nlpcn.jcoder.util;

/**
 * @author shb
 * @createTime 2015年1月26日
 */
public class TimeUtil {

	/**
	 * 将long格式时间转化为:?天?时?分?秒
	 */
	public static String formatTime(long time) {
		StringBuilder sb = new StringBuilder();
		long mSec = time % 1000;
		time /= 1000;
		long year = time / (365 * 24 * 3600);
		if (year != 0) {
			sb.append(year);
			sb.append("年");
		}
		time = time % (365 * 24 * 3600);
		long month = time / (30 * 24 * 3600);
		if (month != 0) {
			sb.append(month);
			sb.append("月");
		}
		time = time % (30 * 24 * 3600);
		long day = time / (24 * 3600);
		if (day != 0) {
			sb.append(day);
			sb.append("日");
		}
		time = time % (24 * 3600);
		long hour = time / 3600;
		if (hour != 0) {
			sb.append(hour);
			sb.append("小时");
		}
		time = time % 3600;
		long min = time / 60;
		if (min != 0) {
			sb.append(min);
			sb.append("分钟");
		}
		time = time % 60;
		long sec = time;
		if (sec != 0) {
			sb.append(sec);
			sb.append("秒");
		}
		if (mSec != 0) {
			sb.append(mSec);
			sb.append("毫秒");
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		System.err.println(formatTime(23312324));
	}
}
