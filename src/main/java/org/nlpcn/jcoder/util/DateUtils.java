package org.nlpcn.jcoder.util;

import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtils {
	public static final String SDF_STANDARD = "yyyy-MM-dd HH:mm:ss";
	public static final String SDF_TIMESTAP = "yyyyMMddHHmmss";
	public static final String SDF_YYYYMMDDHH = "yyyy-MM-dd HH";
	public static final String SDF_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String SDF_LONG = "yyyy-MM-dd HH:mm:ss,SSS";
	public static final String SDF_SHORT = "yyyy-MM-dd";
	public static final String SDF_YYYYMMDD = "yyyyMMdd";
	public static final String SDF_YEAR = "yyyy";
	public static final String SDF_HHMM = "HH:mm";
	public static final String SDF_MMDD_ZH = "MM月dd日";
	public static final String SDF_MONTH = "yyyy-MM";

	/**
	 * 锁对象
	 */
	private static final Object lockObj = new Object();

	/**
	 * 存放不同的日期模板格式的sdf的Map
	 */
	private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

	/**
	 * 返回一个ThreadLocal的sdf,每个线程只会new一次sdf
	 *
	 * @param pattern
	 * @return
	 */
	private static SimpleDateFormat getSdf(final String pattern) {
		ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);

		// 此处的双重判断和同步是为了防止sdfMap这个单例被多次put重复的sdf
		if (tl == null) {
			synchronized (lockObj) {
				tl = sdfMap.get(pattern);
				if (tl == null) {
					// 这里是关键,使用ThreadLocal<SimpleDateFormat>替代原来直接new
					// SimpleDateFormat
					tl = new ThreadLocal<SimpleDateFormat>() {
						@Override
						protected SimpleDateFormat initialValue() {
							return new SimpleDateFormat(pattern);
						}
					};
					sdfMap.put(pattern, tl);
				}
			}
		}

		return tl.get();
	}

	/**
	 * 将date转为今日0点
	 *
	 * @return
	 */
	public static Date zeroDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 将date时间转换为整小时
	 *
	 * @return
	 */
	public static Date zeroHour(long time) {
		return zeroHour(new Date(time));
	}


	/**
	 * 将date时间转换为整小时
	 *
	 * @return
	 */
	public static Date zeroHour(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 将date转为今日0点
	 *
	 * @return
	 */
	public static Date zeroDate(long time) {
		return zeroDate(new Date(time));
	}

	public static String formatDate(long time, String sdf) {
		return formatDate(new Date(time), sdf);
	}

	public static String formatDate(Date date, String sdf) {
		String dateString = "";
		try {
			return getSdf(sdf).format(date);
		} catch (Exception e) {
		}
		return dateString;
	}

	public static String formatDate(String dateString) {
		if (dateString != null) {
			return dateString.replaceAll("-", "");
		} else {
			return "";
		}
	}

	public static Date getDate(String dateString, String sdf) {
		try {
			return getSdf(sdf).parse(dateString);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String formatDate(String dateString, String sdf1, String sdf2) {
		try {
			Date date = getSdf(sdf1).parse(dateString);
			return getSdf(sdf2).format(date);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 增加分钟
	 *
	 * @param date
	 * @param mi
	 * @return
	 * @author zhang_zg
	 */
	public static Date addMinutes(Date date, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, minute);
		date = calendar.getTime();
		return date;
	}

	/**
	 * @param date 日期
	 * @param days 前几天
	 * @return
	 */
	public static Date getNextDay(Date date, int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, days);
		date = calendar.getTime();
		return date;
	}

	/**
	 * 增加月份
	 *
	 * @param date
	 * @param months
	 * @return
	 * @author zhang_zg
	 */
	public static Date addMonths(Date date, int months) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, months);
		date = calendar.getTime();
		return date;
	}

	/**
	 * 获取两个日期之间的时间差，单位 秒
	 *
	 * @param start
	 * @param end
	 * @return
	 * @author zhang_zg
	 */
	public static long getDateDiff(Date start, Date end) {
		return (end.getTime() - start.getTime()) / 1000;
	}

	public static Date getDate(long t) {
		Date dat = new Date(t);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(dat);
		return gc.getTime();
	}

	public static boolean isYesterday(String dateString) {
		dateString = DateUtils.formatDate(dateString);
		Date yesterday = getNextDay(new Date(), -1);
		return dateString.equals(formatDate(yesterday, SDF_YYYYMMDD));
	}

	public static int getYear(Date date) {
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		return c.get(Calendar.YEAR);
	}

	public static int getMonth(Date date) {
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		return c.get(Calendar.MONTH) + 1;
	}

	/**
	 * 获得某一个时间的0点
	 *
	 * @param date
	 * @return
	 */
	public static Date todayZero(Date date) {
		return date;
	}
}
