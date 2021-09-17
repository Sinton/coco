package com.github.coco.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * @author Yan
 */
public class JsonHelper {
    /**
     * JSON字符串转Map集合
     *
     * @param resource
     * @return
     */
    public static <K, V> Map<K, V> jsonStringConvertMap(String resource) {
        return JSON.parseObject(resource, new TypeReference<Map<K, V>>() {});
    }

    /**
     * JSON字符串转List集合
     *
     * @param resource
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonStringConvertList(String resource) {
        return JSON.parseObject(resource, new TypeReference<List<T>>() {});
    }

    /**
     * JSON字符串转Java对象
     *
     * @param resource
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T jsonStringConvertObject(String resource, Class<T> clazz) {
        return JSON.parseObject(resource, clazz);
    }
}
