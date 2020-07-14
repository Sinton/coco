package com.github.coco.controller;

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
@RequestMapping(value = "/api/swarm")
public class SwarmController extends BaseController {

    @PostMapping(value = "/inspect")
    public Map<String, Object> getSwarm(@RequestBody Map<String, Object> params) {
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, dockerClient.inspectSwarm());
        } catch (Exception e) {
            LoggerHelper.fmtError(this.getClass(), e, "获取集群节点信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "获取集群节点信息失败");
        }
    }
}
