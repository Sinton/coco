package com.github.coco.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yan
 */
public class MapHelper {

    /**
     * 获取两个Map集合的交集
     *
     * @param left
     * @param right
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> getInnerJoinMap(Map<K, V> left, Map<K, V> right) {
        Map<K, V> inner = new HashMap<>(16);
        left.forEach((k, v) -> {
            if (right.containsKey(k)) {
                inner.put(k, v);
            }
        });
        return inner;
    }

    /**
     * 获取两个Map集合中，除右集合元素外，左集合的差集
     *
     * @param left
     * @param right
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> getLeftJoinMap(Map<K, V> left, Map<K, V> right) {
        Map<K, V> leftJoin = new HashMap<>(16);
        leftJoin.putAll(left);
        right.forEach(leftJoin::remove);
        return leftJoin;
    }

    /**
     * 获取两个Map集合中，除左集合元素外，右集合的差集
     *
     * @param left
     * @param right
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> getRightJoinMap(Map<K, V> left, Map<K, V> right) {
        Map<K, V> rightJoin = new HashMap<>(16);
        rightJoin.putAll(right);
        left.forEach(rightJoin::remove);
        return rightJoin;
    }

    /**
     * 获取两个Map集合的并集
     *
     * @param left
     * @param right
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> getUnionAllMap(Map<K, V> left, Map<K, V> right) {
        return getUnionAllMap(left, right, true);
    }

    public static <K, V> Map<K, V> getUnionAllMap(Map<K, V> left, Map<K, V> right, boolean cover) {
        if (cover) {
            left.putAll(right);
        } else {
            right.forEach(left::putIfAbsent);
        }
        return left;
    }

    /**
     * Map集合中的主键
     *
     * @param in
     * @param args
     * @param <K> Map集合中的主键
     * @param <V> Map集合中的映射值
     * @return
     */
    public static <K, V> Map<K, V> getSelectMap(Map<K, V> in, K... args) {
        return getSelectMap(in, Arrays.asList(args));
    }

    public static <K, V> Map<K, V> getSelectMap(Map<K, V> in, List<K> args) {
        Map<K, V> out = new HashMap<>(16);
        args.forEach(item -> out.put(item, in.get(item)));
        return out;
    }
}
