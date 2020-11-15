package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.User;
import com.github.coco.service.UserService;
import com.github.coco.utils.EncryptHelper;
import com.github.coco.utils.LoggerHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    @WebLog
    @PostMapping(value = "/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> params) {
        try {
            String username = Objects.toString(params.get("username"), "");
            String password = Objects.toString(params.get("password"), "");
            String salt = EncryptHelper.md5(Long.toString(System.currentTimeMillis()));
            if (StringUtils.isAllBlank(username, password)) {
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "用户名或密码不能为空");
            }
            User user = new User();
            user.setUsername(username);
            user.setNickname(username);
            user.setSalt(salt);
            user.setPassword(EncryptHelper.md5(password + salt));
            user.setCreateTime(System.currentTimeMillis());
            userService.createUser(user);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "注册用户成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "注册用户失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "注册用户失败");
        }
    }

    @WebLog
    @GetMapping(value = "/profile")
    public Map<String, Object> profile(@RequestParam("username") String username) {
        Map<String, Object> result = new HashMap<>(16);
        User user = userService.getUserByName(username);
        result.putAll(JSON.parseObject(JSON.toJSONString(user), new TypeReference<HashMap<String, Object>>() {}));
        result.put("merchantCode", "TLif2btpzg079h15bk");
        result.put("deleted", "0");
        result.put("roleId", "admin");
        result.put("role", "{}");
        result.put("token", "'4291d7da9005377ec9aec4a71ea837f'");
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, result, "result");
    }
}
