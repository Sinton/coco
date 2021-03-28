package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.SwarmInit;
import com.spotify.docker.client.messages.swarm.SwarmJoin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * @author Yan
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/swarm")
public class SwarmController extends BaseController {

    @WebLog
    @PostMapping(value = "/init")
    public Map<String, Object> initSwarm(@RequestBody Map<String, Object> params) {
        String ip = Objects.toString(params.get("ip"), "localhost");
        try {
            if (getDockerClient().info().swarm() == null || !getDockerClient().info().swarm().controlAvailable()) {
                SwarmInit swarmInit = SwarmInit.builder()
                                               .advertiseAddr(ip)
                                               .listenAddr(ip)
                                               .build();
                getDockerClient().initSwarm(swarmInit);
            } else {
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "初始化Swarm集群节点成功");
            }
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "已Swarm集群化");
        } catch (Exception e) {
            log.error("初始化Swarm集群节点失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "初始化Swarm集群节点失败");
        }
    }

    @WebLog
    @PostMapping(value = "/join")
    public Map<String, Object> joinSwarm(@RequestBody Map<String, Object> params) {
        try {
            String joinToken = getDockerClient().inspectSwarm().joinTokens().worker();
            SwarmJoin swarmJoin = SwarmJoin.builder()
                                           .joinToken(joinToken)
                                           .build();
            getDockerClient().joinSwarm(swarmJoin);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "");
        } catch (DockerException | InterruptedException e) {
            log.error("集群节点创建失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "集群节点创建失败");
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> getSwarm(@RequestBody Map<String, Object> params) {
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, getDockerClient().inspectSwarm());
        } catch (Exception e) {
            log.error("获取集群节点信息失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "获取集群节点信息失败");
        }
    }
}
