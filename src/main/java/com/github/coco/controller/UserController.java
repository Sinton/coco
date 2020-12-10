package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.User;
import com.github.coco.service.UserService;
import com.github.coco.utils.EncryptHelper;
import com.github.coco.utils.JwtHelper;
import com.github.coco.utils.LoggerHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/user")
public class UserController extends BaseController {
    @Resource
    private UserService userService;

    @Value("classpath:mock/role1.json")
    private org.springframework.core.io.Resource roleRes;

    @WebLog
    @PostMapping(value = "/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> params) {
        try {
            String username = Objects.toString(params.get("username"), "");
            String password = Objects.toString(params.get("password"), "");
            if (StringUtils.isAllBlank(username, password)) {
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "用户名或密码不能为空");
            }
            User user = userService.getUserByName(username);
            if (user != null) {
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "用户名已存在");
            } else {
                String salt = EncryptHelper.md5(Long.toString(System.currentTimeMillis()));
                user = new User();
                user.setUsername(username);
                user.setNickname(username);
                user.setSalt(salt);
                user.setPassword(EncryptHelper.md5(EncryptHelper.md5(password) + salt));
                user.setCreateTime(System.currentTimeMillis());
                userService.createUser(user);
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "注册用户成功");
            }
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "注册用户失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "注册用户失败");
        }
    }

    @WebLog
    @GetMapping(value = "/profile")
    public Map<String, Object> profile(@RequestHeader("Authorization") String token) throws IOException {
        String username = JwtHelper.getUsername(token);
        User user = userService.getUserByName(username);
        Map<String, Object> result = new HashMap<>(16);
        result.putAll(JSON.parseObject(JSON.toJSONString(user), new TypeReference<HashMap<String, Object>>() {}));
        result.put("merchantCode", "TLif2btpzg079h15bk");
        result.put("deleted", "0");
        result.put("roleId", "admin");

        Map<String, Object> role = JSON.parseObject(IOUtils.toString(roleRes.getInputStream(), StandardCharsets.UTF_8),
                                                    new TypeReference<HashMap<String, Object>>() {});
        result.put("role", role);
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, result);
    }
}
