package com.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    // 默认日期时间格式
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // 默认日期格式
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    // 默认时间格式
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 将时间戳转换为时间字符串
     * @param timestamp 时间戳
     * @param format 时间格式
     * @return 时间字符串
     */
    public static String timestampToString(long timestamp, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date(timestamp));
    }

    /**
     * 将时间字符串转换为时间戳
     * @param timeStr 时间字符串
     * @param format 时间格式
     * @return 时间戳
     * @throws ParseException 时间格式错误
     */
    public static long stringToTimestamp(String timeStr, String format) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = dateFormat.parse(timeStr);
        return date.getTime();
    }

    /**
     * 返回当前时间的时间戳
     * @return 当前时间的时间戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 返回当前时间的时间字符串
     * @param format 时间格式
     * @return 当前时间的时间字符串
     */
    public static String getCurrentTimeString(String format) {
        return timestampToString(getCurrentTimestamp(), format);
    }
}

