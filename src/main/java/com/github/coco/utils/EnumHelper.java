package com.github.coco.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author Yan
 */
@Slf4j
public class EnumHelper {
    private static final String MATCH_METHOD  = "getCode";
    private static final String VALUES_METHOD = "values";

    /**
     * 获取枚举类型
     *
     * @param enumClass
     * @param enumValue
     * @param <T>
     * @return
     */
    public static <T> T getEnumType(Class<T> enumClass, String enumValue) {
        return getEnumType(enumClass, enumValue, null);
    }

    public static <T> T getEnumType(Class<T> enumClass, String enumValue, T defaultEnum) {
        return getEnumType(enumClass, enumValue, defaultEnum, MATCH_METHOD);
    }

    public static <T> T getEnumType(Class<T> enumClass, String enumValue, T defaultEnum, String matchMethod) {
        Enum<?>[] enums = getEnums(enumClass);
        if (enums != null) {
            return getEnumType(enums, enumValue, defaultEnum, matchMethod == null ? MATCH_METHOD : matchMethod);
        }
        return defaultEnum;
    }

    private static <T> T getEnumType(Enum<?>[] enumClass, String enumValue, T defaultEnum, String matchMethod) {
        try {
            Method method;
            Object enumCode;
            for (Enum<?> enumItem : enumClass) {
                method   = enumItem.getClass().getMethod(matchMethod);
                enumCode = method.invoke(enumItem);
                if (enumCode.toString().equals(enumValue)) {
                    return (T) enumItem;
                }
            }
        } catch (Exception e) {
            log.error("获取枚举项时失败", e);
            return defaultEnum;
        }
        return defaultEnum;
    }

    /**
     * 获取枚举类中的枚举项
     *
     * @param enumClass
     * @return
     */
    private static <T> Enum<?>[] getEnums(Class<T> enumClass) {
        Enum<?>[] enums;
        try {
            Method method = enumClass.getMethod(VALUES_METHOD);
            enums         = (Enum<?>[]) method.invoke(enumClass);
        } catch (Exception e) {
            log.error(String.format("获取%s枚举项失败", enumClass.getName()), e);
            return null;
        }
        return enums;
    }
}
