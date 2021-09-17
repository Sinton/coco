package com.github.coco.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author Yan
 */
public class DateHelper {
    /**
     * 一纳米
     */
    public static final long NANOSECOND  = 1L;
    /**
     * 一微秒
     */
    public static final long MICROSECOND = NANOSECOND * 1000L;
    /**
     * 一毫秒
     */
    public static final long MILLISECOND = MICROSECOND * 1000L;
    /**
     * 一秒钟
     */
    public static final long SECOND      = MILLISECOND * 1000L;
    /**
     * 一分钟
     */
    public static final long MINUTE      = SECOND * 60L;
    /**
     * 一小时
     */
    public static final long HOUR        = MINUTE * 60L;
    /**
     * 一天
     */
    public static final long DAY         = HOUR * 24L;
    /**
     * 纳米时间单位
     */
    public static final String NANOSECOND_UNIT = "ns";
    /**
     * 微秒时间单位
     */
    public static final String MICROSECOND_UNIT = "us";
    /**
     * 毫秒时间单位
     */
    public static final String MILLISECOND_UNIT = "ms";
    /**
     * 秒钟时间单位
     */
    public static final String SECOND_UNIT = "s";
    /**
     * 分钟时间单位
     */
    public static final String MINUTE_UNIT = "m";
    /**
     * 小时时间单位
     */
    public static final String HOUR_UNIT = "h";

    /**
     * 获取时间单位
     *
     * @param str
     * @return
     */
    public static String getTimeUnit(String str) {
        if (str.endsWith(NANOSECOND_UNIT)) {
            return NANOSECOND_UNIT;
        } else if (str.endsWith(MICROSECOND_UNIT)) {
            return MICROSECOND_UNIT;
        } else if (str.endsWith(MILLISECOND_UNIT)) {
            return MILLISECOND_UNIT;
        } else if (str.endsWith(SECOND_UNIT)) {
            return SECOND_UNIT;
        } else if (str.endsWith(MINUTE_UNIT)) {
            return MINUTE_UNIT;
        } else if (str.endsWith(HOUR_UNIT)) {
            return HOUR_UNIT;
        } else {
            return SECOND_UNIT;
        }
    }

    /**
     * 根据时间单位获取纳米级时间
     *
     * @param str
     * @return
     */
    public static long getNanosTimeByUnit(String str) {
        switch (DateHelper.getTimeUnit(str)) {
            case DateHelper.NANOSECOND_UNIT:
                return TimeUnit.NANOSECONDS.toNanos(Long.parseLong(StringUtils.removeEnd(str,
                                                                                         DateHelper.NANOSECOND_UNIT)));
            case DateHelper.MICROSECOND_UNIT:
                return TimeUnit.MICROSECONDS.toNanos(Long.parseLong(StringUtils.removeEnd(str,
                                                                                          DateHelper.MICROSECOND_UNIT)));
            case DateHelper.MILLISECOND_UNIT:
                return TimeUnit.MILLISECONDS.toNanos(Long.parseLong(StringUtils.removeEnd(str,
                                                                                          DateHelper.MILLISECOND_UNIT)));
            case DateHelper.SECOND_UNIT:
                return TimeUnit.SECONDS.toNanos(Long.parseLong(StringUtils.removeEnd(str,
                                                                                     DateHelper.SECOND_UNIT)));
            case DateHelper.MINUTE_UNIT:
                return TimeUnit.MINUTES.toNanos(Long.parseLong(StringUtils.removeEnd(str,
                                                                                     DateHelper.MINUTE_UNIT)));
            case DateHelper.HOUR_UNIT:
                return TimeUnit.HOURS.toNanos(Long.parseLong(StringUtils.removeEnd(str,
                                                                                   DateHelper.HOUR_UNIT)));
            default:
                return 0L;
        }
    }
}
