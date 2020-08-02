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
@RequestMapping(value = "/api/docker")
public class DockerController extends BaseController {

    @WebLog
    @PostMapping(value = "/version")
    public Map<String, Object> getDockerVersion(@RequestBody Map<String, Object> params) {
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, dockerClient.version());
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取Docker版本内容失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/info")
    public Map<String, Object> getDockerInfo(@RequestBody Map<String, Object> params) {
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, dockerClient.info());
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取Docker信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
