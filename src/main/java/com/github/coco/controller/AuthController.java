package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.cache.GlobalCache;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.User;
import com.github.coco.service.UserService;
import com.github.coco.utils.EncryptHelper;
import com.github.coco.utils.LoggerHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/auth")
public class AuthController extends BaseController {
    @Resource
    private UserService userService;

    @WebLog
    @PostMapping(value = "/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> params) {
        String username = params.get("username").toString();
        String password = params.get("password").toString();
        Map<String, Object> result = new HashMap<>(16);
        try {
            User user = userService.getUserByName(username);
            if (user != null) {
                if (user.getPassword().equals(EncryptHelper.md5(password + user.getSalt()))) {
                    String token = UUID.randomUUID().toString();
                    globalCache.getCache(GlobalCache.CacheTypeEnum.TOKEN).put(token, user);
                    result.putAll(JSON.parseObject(JSON.toJSONString(user), new TypeReference<Map<String, Object>>() {}));
                    result.put("deleted", "0");
                    result.put("roleId", "admin");
                    result.put("token", token);
                    return apiResponseDTO.returnResult(ErrorCodeEnum.SUCCESS.getCode(), result);
                } else {
                    return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "用户密码错误");
                }
            } else {
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "用户不存在");
            }
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "登陆失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "登陆失败");
        }
    }

    @WebLog
    @PostMapping(value = "/logout")
    public Map<String, Object> logout() {
        evictToken();
        return apiResponseDTO.returnResult(ErrorCodeEnum.SUCCESS.getCode(), "注销成功");
    }

    @WebLog
    @PostMapping(value = "/smsCaptcha")
    public Map<String, Object> smsCaptcha() {
        return apiResponseDTO.returnResult(ErrorCodeEnum.SUCCESS.getCode(), "");
    }

    @WebLog
    @PostMapping(value = "/twoFactor")
    public Map<String, Object> twofactor() {
        return apiResponseDTO.returnResult(ErrorCodeEnum.SUCCESS.getCode(), "");
    }
}
