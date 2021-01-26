package com.github.coco.utils;

import com.github.coco.constant.GlobalConstant;
import com.github.coco.core.RuntimeContext;

/**
 * @author Yan
 */
public class RuntimeContextHelper {
    public static String getToken() {
        return String.valueOf(get(GlobalConstant.TOKEN));
    }

    public static Object get(String key) {
        return RuntimeContext.getContext().get(key);
    }

    public static Object get() {
        return RuntimeContext.getContext().get();
    }
}
