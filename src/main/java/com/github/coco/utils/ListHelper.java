package com.github.coco.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yan
 */
public class ListHelper {
    public static <T> List<T> getInnerJoinList(List<T> left, List<T> right) {
        List<T> inner = new ArrayList<>();
        left.forEach(item -> {
            if (right.contains(item)) {
                inner.add(item);
            }
        });
        return inner;
    }

    public static <T> List<T> getLeftJoinList(List<T> left, List<T> right) {
        List<T> leftJoin = new ArrayList<>();
        leftJoin.addAll(left);
        right.forEach(leftJoin::remove);
        return leftJoin;
    }

    public static <T> List<T> getRightJoinList(List<T> left, List<T> right) {
        List<T> rightJoin = new ArrayList<>();
        rightJoin.addAll(right);
        left.forEach(rightJoin::remove);
        return rightJoin;
    }
}
