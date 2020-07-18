package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.LoggerHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/user")
public class UserController extends BaseController {

    @WebLog
    @PostMapping(value = "/signin")
    public Map<String, Object> signin(@RequestBody Map<String, Object> params) {
        String username = params.get("username").toString();
        String password = params.get("password").toString();
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "登陆成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "登陆失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "登陆失败");
        }
    }

    @WebLog
    @PostMapping(value = "/signout")
    public Map<String, Object> signout(@RequestBody Map<String, Object> params) {
        String username = params.get("username").toString();
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "登陆成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "登陆失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "登陆失败");
        }
    }
}
