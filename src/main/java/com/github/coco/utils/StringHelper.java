package com.github.coco.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.Map;

/**
 * @author Yan
 */
public class StringHelper {
    private static final String[] CAPACITY_UNITS = {"Byte", "KB", "MB", "GB"};

    /**
     * 字符串转Map集合
     *
     * @param resource
     * @return
     */
    public static <K, V> Map<K, V> stringConvertMap(String resource) {
        return JSON.parseObject(resource, new TypeReference<Map<K, V>>() {});
    }

    /**
     * 转换带单位的容量
     *
     * @param size
     * @return
     */
    public static String convertSize(double size) {
        return convertSize(size, 1000);
    }

    public static String convertSize(double size, int base) {
        // 容量等级
        int level = (int) Math.floor(Math.log(size) / Math.log(base));
        // 当前容量
        double capacity = size / Math.pow(base, level);
        // 四舍五入
        double currSize = Math.round(capacity * 100) / 100.0;
        return String.format("%s %s", currSize, CAPACITY_UNITS[level]);
    }
}
