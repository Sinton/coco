package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.messages.swarm.SwarmInit;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/swarm")
public class SwarmController extends BaseController {

    @WebLog
    @PostMapping(value = "/init")
    public Map<String, Object> initSwarm(@RequestBody Map<String, Object> params) {
        String ip = Objects.toString(params.get("ip"), "192.168.3.140");
        try {
            if (dockerClient.info().swarm() == null) {
                SwarmInit swarmInit = SwarmInit.builder()
                                               .advertiseAddr(ip)
                                               .listenAddr(ip)
                                               .build();
                dockerClient.initSwarm(swarmInit);
            } else {
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "初始化Swarm集群节点成功");
            }
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "已Swarm集群化");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "初始化Swarm集群节点失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "初始化Swarm集群节点失败");
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> getSwarm(@RequestBody Map<String, Object> params) {
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, dockerClient.inspectSwarm());
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取集群节点信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "获取集群节点信息失败");
        }
    }
}
