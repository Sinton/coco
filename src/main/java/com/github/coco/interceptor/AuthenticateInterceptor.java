package com.github.coco.interceptor;

import com.alibaba.fastjson.JSON;
import com.github.coco.cache.GlobalCache;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.core.AppContext;
import com.github.coco.dto.ApiResponseDTO;
import com.github.coco.entity.User;
import com.github.coco.utils.LoggerHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @author Yan
 */
@Component
public class AuthenticateInterceptor implements HandlerInterceptor {
    private GlobalCache globalCache = AppContext.getBean(GlobalCache.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行OPTIONS请求
        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 判断token是否为空是否有效
        String token = request.getHeader(GlobalConstant.ACCESS_TOKEN);
        if (StringUtils.isNotBlank(token)) {
            if (globalCache.getCache(GlobalCache.CacheTypeEnum.TOKEN).get(token, User.class) != null) {
                return true;
            } else {
                printJson(response);
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

    private void printJson(HttpServletResponse response) {
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String content = JSON.toJSONString(apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "token过期,请重新登陆"));
        printContent(response, content);
    }

    private void printContent(HttpServletResponse response, String content) {
        try {
            response.reset();
            response.setContentType("application/json");
            response.setHeader("Cache-Control", "no-store");
            response.setCharacterEncoding("UTF-8");
            PrintWriter pw = response.getWriter();
            pw.write(content);
            pw.flush();
        } catch (Exception e) {
            LoggerHelper.fmtError(AuthenticateInterceptor.class, e, "");
        }
    }
}
