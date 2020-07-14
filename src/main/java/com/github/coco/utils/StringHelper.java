package com.github.coco.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.Map;

/**
 * @author Yan
 */
public class StringHelper {
    /**
     * 字符串转Map集合
     *
     * @param resource
     * @return
     */
    public static Map<String, Object> stringConvertMap(String resource) {
        return JSON.parseObject(resource, new TypeReference<Map<String, Object>>() {});
    }
}
