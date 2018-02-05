package org.nlpcn.jcoder.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Arith {
	// 默认除法运算精度
	private static final int DEF_DIV_SCALE = 10;

	// 这个类不能实例化
	private Arith() {
		;
	}

	/** */
	/**
	 * 提供精确的加法运算。
	 *
	 * @param v1 被加数
	 * @param v2 加数
	 * @return 两个参数的和
	 */
	public static double add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}

	/** */
	/**
	 * 提供精确的减法运算。
	 *
	 * @param v1 被减数
	 * @param v2 减数
	 * @return 两个参数的差
	 */
	public static double sub(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2).doubleValue();
	}

	/** */
	/**
	 * 提供精确的乘法运算。
	 *
	 * @param v1 被乘数
	 * @param v2 乘数
	 * @return 两个参数的积
	 */
	public static double mul(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2).doubleValue();
	}

	/** */
	/**
	 * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
	 *
	 * @param v1 被除数
	 * @param v2 除数
	 * @return 两个参数的商
	 */
	public static double div(double v1, double v2) {
		return div(v1, v2, DEF_DIV_SCALE);
	}

	/** */
	/**
	 * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
	 *
	 * @param v1 被除数
	 * @param v2 除数
	 * @return 两个参数的商
	 */
	public static double divWithOutZero(double v1, double v2) {
		if (v1 == 0 || v2 == 0) {
			return 0;
		}
		return div(v1, v2, DEF_DIV_SCALE);
	}

	/** */
	/**
	 * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。
	 *
	 * @param v1    被除数
	 * @param v2    除数
	 * @param scale 表示表示需要精确到小数点以后几位。
	 * @return 两个参数的商
	 */
	public static double div(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/** */
	/**
	 * 提供精确的小数位四舍五入处理。
	 *
	 * @param v     需要四舍五入的数字
	 * @param scale 小数点后保留几位
	 * @return 四舍五入后的结果
	 */
	public static double round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 指定保留n位小数
	 *
	 * @param str
	 * @param keepType   "#0.##" 假如两位
	 * @param keepLength ".00" 假如两位
	 * @return
	 */
	public static String formatDouble(Double str, String keepType, String keepLength) {
		// 定义一个数字格式化对象，格式化模板为".#"，即保留1位小数.
		DecimalFormat b = new DecimalFormat(keepType);
		b.applyPattern(keepLength);
		b.setRoundingMode(RoundingMode.FLOOR); // 不四舍五入
		String resultData = b.format(str);
		return resultData;
	}

	/**
	 * 归一
	 *
	 * @param <T>
	 * @param map
	 * @return
	 */
	public static <T> Map<T, Double> normalized(Map<T, Double> map) {
		double sum = 0;
		for (Double v : map.values()) {
			if (v.isInfinite() || v.isNaN()) {
				v = 0.0;
			}
			sum += v;
		}

		for (T k : map.keySet()) {

			Double v = map.get(k) / sum;
			if (v.isInfinite() || v.isNaN()) {
				v = 0.0;
			}
			map.put(k, v);
		}

		return map;
	}

	/**
	 * 归一
	 *
	 * @param <T>
	 * @param map
	 * @return
	 */
	public static List<Double> normalized(List<Double> all) {
		double sum = 0;
		for (Double v : all) {
			if (v == null || v.isInfinite() || v.isNaN()) {
				v = 0.0;
			}
			sum += v;
		}

		List<Double> result = new ArrayList<>();
		for (Double v : all) {

			result.add(v / sum);
		}

		return result;
	}
}
