package org.nlpcn.jcoder.util;

public class StringUtil {

    public static final String EMPTY = "";

    private static final String NULL = "null";

    /**
     * 判断字符串是否为空
     *
     * @param cs
     * @return
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否不为空
     *
     * @param cs
     * @return
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return NULL;
        } else {
            return obj.toString();
        }
    }

    public static boolean isBlank(char[] chars) {
        // TODO Auto-generated method stub
        int strLen;
        if (chars == null || (strLen = chars.length) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(chars[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * trim 一个字符串.扩展了string类原生的trim.对BOM和中文空格进行trim
     *
     * @return
     */
    public static String trim(String value) {

        if (value == null) {
            return null;
        }

        int len = value.length();

        int st = 0;

        while ((st < len) && (Character.isWhitespace(value.charAt(st)) || value.charAt(st) == 65279 || value.charAt(st) == 160 || value.charAt(st) == 12288)) {
            st++;
        }
        while ((st < len) && (Character.isWhitespace(value.charAt(len - 1)) || value.charAt(st) == 160 || value.charAt(st) == 12288)) {
            len--;
        }
        return ((st > 0) || (len < value.length())) ? value.substring(st, len) : value;
    }
}
