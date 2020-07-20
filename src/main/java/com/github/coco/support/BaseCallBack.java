package com.github.coco.support;

/**
 * @author Yan
 */
public interface BaseCallBack {
    /**
     * 回调方法
     *
     * @param args
     * @param <T>
     * @return
     */
    <T, K> T invoke(K... args);
}
