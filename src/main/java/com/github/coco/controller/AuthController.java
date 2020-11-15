package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.User;
import com.github.coco.service.UserService;
import com.github.coco.utils.DockerConnectorHelper;
import com.github.coco.utils.JwtHelper;
import com.github.coco.utils.LoggerHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> response = new HashMap<>(16);
        try {
            User user = userService.getUserByName(username);
            if (user != null) {
                String jwt = JwtHelper.sign(username);
                Map<String, Object> result = new HashMap<>(16);
                result.putAll(JSON.parseObject(JSON.toJSONString(user), new TypeReference<Map<String, Object>>() {}));
                result.put("deleted", "0");
                result.put("roleId", "admin");
                //result.put("token", "'4291d7da9005377ec9aec4a71ea837f'");
                result.put("token", jwt);
                result.put("jwt", jwt);
                response.put("code", 2000);
                response.put("result", result);
                dockerClient = DockerConnectorHelper.borrowDockerClient("192.168.3.140", 2375);
                dockerClients.put(result.get("token").toString(), dockerClient);
            } else {
            }
            return response;
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "登陆失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "登陆失败");
        }
    }

    @WebLog
    @PostMapping(value = "/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>(2);
        return result;
    }

    @WebLog
    @PostMapping(value = "/smsCaptcha")
    public Map<String, Object> smsCaptcha() {
        Map<String, Object> result = new HashMap<>(2);
        return result;
    }

    @WebLog
    @PostMapping(value = "/twofactor")
    public Map<String, Object> twofactor() {
        Map<String, Object> result = new HashMap<>(2);
        return result;
    }
}
