package com.github.coco.config;

import com.github.coco.interceptor.AuthenticateInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Yan
 */
@Configuration
public class AuthConfig implements WebMvcConfigurer {
    private static final String[] PATH_WHITE_LIST = {"/**/login", "/**/logout", "/**/register"};

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(new AuthenticateInterceptor());
        // 需拦截的路径
        interceptorRegistration.addPathPatterns("/**");
        // 需放行的路径
        interceptorRegistration.excludePathPatterns(PATH_WHITE_LIST);
    }
}
