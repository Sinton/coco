package com.github.coco.utils;

import com.github.coco.core.AppContext;
import org.springframework.core.env.Environment;

/**
 * @author Yan
 */
public class ConfigHelper {
    private static Environment environment = AppContext.getBean(Environment.class);

    public static String getProperty(String key) {
        return environment.getProperty(key);
    }

    public static <T> T getProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }
}
