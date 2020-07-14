package com.github.coco.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yan
 */
public class LoggerHelper {
    public static boolean isInfo  = LoggerFactory.getLogger(LoggerHelper.class).isInfoEnabled();
    public static boolean isError = LoggerFactory.getLogger(LoggerHelper.class).isErrorEnabled();
    public static boolean isDebug = LoggerFactory.getLogger(LoggerHelper.class).isDebugEnabled();

    public static void debug(Class<?> clazz, String message) {
        if (!isDebug) {
            return;
        }
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.debug(message);
    }

    /**
     * 格式化输出，占位符同MessageFormat.format()函数
     *
     * @param clazz
     * @param fmtString
     * @param value
     */
    public static <T> void fmtDebug(Class<?> clazz, String fmtString, T... value) {
        if (!isDebug) {
            return;
        }
        if (StringUtils.isBlank(fmtString)) {
            return;
        }
        if ((null != value) && (value.length != 0)) {
            fmtString = String.format(fmtString, (Object) value);
        }
        debug(clazz, fmtString);
    }

    public static void info(Class<?> clazz, String message) {
        if (!isInfo) {
            return;
        }

        Logger logger = LoggerFactory.getLogger(clazz);
        logger.info(message);
    }

    /**
     * 格式化输出，占位符同String.format()函数
     *
     * @param clazz
     * @param fmtString
     * @param value
     */
    public static <T> void fmtInfo(Class<?> clazz, String fmtString, T... value) {
        if (!isInfo) {
            return;
        }

        if (StringUtils.isBlank(fmtString)) {
            return;
        }
        if ((null != value) && (value.length != 0)) {
            fmtString = String.format(fmtString, (Object) value);
        }
        info(clazz, fmtString);
    }

    public static void error(Class<?> clazz, String message, Exception e) {
        if (!isError) {
            return;
        }

        Logger logger = LoggerFactory.getLogger(clazz);
        if (null == e) {
            logger.error(message);
            return;
        }
        logger.error(message, e);
    }

    public static void error(Class<?> clazz, String message) {
        error(clazz, message, null);
    }

    /**
     * 格式化输出，占位符同String.format()函数
     *
     * @param clazz
     * @param e
     * @param fmtString
     * @param value
     */
    public static <T> void fmtError(Class<?> clazz, Exception e, String fmtString, T... value) {
        if (!isError) {
            return;
        }

        if (StringUtils.isBlank(fmtString)) {
            return;
        }
        if ((null != value) && (value.length != 0)) {
            fmtString = String.format(fmtString, (Object) value);
        }
        error(clazz, fmtString, e);
    }

    /**
     * 格式化输出，占位符同String.format()函数
     *
     * @param clazz
     * @param fmtString
     * @param value
     */
    public static <T> void fmtError(Class<?> clazz, String fmtString, T... value) {
        fmtError(clazz, null, fmtString, value);
    }
}
