package com.github.coco.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yan
 */
public class RuntimeContext {
    private static final ThreadLocal<RuntimeContext> LOCAL = ThreadLocal.withInitial(RuntimeContext::new);
    private final Map<String, Object> values = new ConcurrentHashMap<>(16);

    protected RuntimeContext() {
    }

    public static RuntimeContext getContext() {
        return LOCAL.get();
    }

    public static void removeContext() {
        LOCAL.remove();
    }

    public Map<String, Object> get() {
        return this.values;
    }

    public RuntimeContext set(String key, Object value) {
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
        return this;
    }
    public RuntimeContext remove(String key) {
        this.values.remove(key);
        return this;
    }

    public Object get(String key) {
        return this.values.get(key);
    }
}
