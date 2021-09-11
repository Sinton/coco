package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.DbConstant;
import com.github.coco.constant.DockerConstant;
import com.github.coco.constant.ErrorConstant;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.EndpointStatusEnum;
import com.github.coco.constant.dict.EndpointTypeEnum;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.Endpoint;
import com.github.coco.schedule.SyncEndpointTask;
import com.github.coco.service.EndpointService;
import com.github.coco.utils.docker.DockerConnectorHelper;
import com.github.coco.utils.RuntimeContextHelper;
import com.github.coco.utils.ThreadPoolHelper;
import com.spotify.docker.client.DockerClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yan
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/endpoint")
public class EndpointController extends BaseController {
    @Resource
    private EndpointService endpointService;

    @Resource
    private SyncEndpointTask syncEndpointTask;

    @WebLog
    @PostMapping("/create")
    public Map<String, Object> createEndpoint(@RequestBody Map<String, Object> params) {
        String name = (String) params.get("name");
        String publicIp = (String) params.get("publicIp");
        String endpointUrl = (String) params.get("endpointUrl");
        // 检查是否已存在
        Endpoint endpoint = endpointService.getEndpoint(Endpoint.builder().publicIp(publicIp).owner(getUserId()).build());
        if (endpoint == null) {
            Endpoint.EndpointBuilder endpointBuilder = Endpoint.builder()
                                                               .name(name)
                                                               .publicIp(publicIp)
                                                               .endpointUrl(endpointUrl)
                                                               .updateDateTime(System.currentTimeMillis())
                                                               .owner(getUserId());
            if (endpointUrl.startsWith(DockerConstant.DEFAULT_UNIX_URI)) {
                endpointBuilder.endpointType(EndpointTypeEnum.UNIX.getCode());
            } else {
                int port = Integer.parseInt(endpointUrl.substring(endpointUrl.indexOf(GlobalConstant.SYMBOL_MARK_COLON) + GlobalConstant.SYMBOL_MARK_COLON.length()));
                endpointBuilder = endpointBuilder.port(port);
                endpointBuilder.endpointType(EndpointTypeEnum.URL.getCode());
            }
            try {
                endpoint = endpointBuilder.build();
                endpointService.createEndpoint(endpoint);
                // 异步更新服务终端信息
                ThreadPoolHelper.provideThreadPool(ThreadPoolHelper.ProvideModeEnum.SINGLE).execute(() -> syncEndpointTask.syncEndpoints());
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, endpointService.getEndpoint(endpoint));
            } catch (Exception e) {
                log.error("创建终端服务发生异常", e);
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "创建终端服务发生异常");
            }
        } else {
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "创建终端服务失败，服务终端已存在");
        }
    }

    @WebLog
    @PostMapping("/remove")
    public Map<String, Object> removeEndpoint(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        try {
            Endpoint endpoint = endpointService.getEndpoint(Endpoint.builder().id(id).build());
            DockerClient dockerClient = getDockerClient();
            DockerConnectorHelper.returnDockerClient(endpoint, dockerClient);
            endpointService.removeEndpoint(id);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除服务终端成功");
        } catch (Exception e) {
            log.error("删除服务终端失败", e);
            return apiResponseDTO.returnResult(ErrorConstant.ERR_BASE_COMMON, "删除服务终端失败");
        }
    }

    @WebLog
    @PostMapping("/modify")
    public Map<String, Object> modifyEndpoint(@RequestBody Map<String, Object> params) {
        try {
            Endpoint endpoint = JSON.parseObject(JSON.toJSONString(params), Endpoint.class);
            Endpoint dbEndpoint = endpointService.getEndpoint(Endpoint.builder().id(endpoint.getId()).build());
            boolean changedEndpointUrl = !dbEndpoint.getEndpointUrl().equals(endpoint.getEndpointUrl());
            if (changedEndpointUrl) {
                // 归还旧Docker连接对象并在上下文中将其移除
                DockerClient dockerClient = getDockerClient();
                if (dockerClient != null) {
                    globalCache.removeDockerClient(RuntimeContextHelper.getToken());
                    DockerConnectorHelper.returnDockerClient(dbEndpoint, dockerClient);
                }
                DockerConnectorHelper.clenrDockerClient(endpoint);
                // 移除上下文中的终端对象
                Endpoint contextEndpoint = getEndpoint();
                if (contextEndpoint != null && contextEndpoint.getId().equals(endpoint.getId())) {
                    globalCache.removeEndpoint(RuntimeContextHelper.getToken());
                }
            }
            BeanUtils.copyProperties(endpoint, dbEndpoint, Endpoint.class);
            dbEndpoint.setUpdateDateTime(System.currentTimeMillis());
            dbEndpoint.setStatus(EndpointStatusEnum.DOWN.getCode());
            endpointService.modifyEndpoint(dbEndpoint);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "修改服务终端成功");
        } catch (Exception e) {
            log.error("修改服务终端失败", e);
            return apiResponseDTO.returnResult(ErrorConstant.ERR_BASE_COMMON, "修改服务终端失败");
        }
    }

    @WebLog
    @PostMapping("/list")
    public Map<String, Object> getPageEndpoints(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(Objects.toString(params.getOrDefault("pageNo", DbConstant.PAGE_NO)));
        int pageSize = Integer.parseInt(Objects.toString(params.getOrDefault("pageSize", DbConstant.PAGE_SIZE)));
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                           apiResponseDTO.tableResult(pageNo,
                                                                      pageSize,
                                                                      endpointService.getEndpoints(pageNo, pageSize)));
    }

    @WebLog
    @PostMapping("/switch")
    public Map<String, Object> switchEndpoint(@RequestBody Map<String, Object> params) {
        String id = Objects.toString(params.get("id"), "");
        if (StringUtils.isNotBlank(id)) {
            Endpoint endpoint = endpointService.getEndpoint(Endpoint.builder().id(Integer.parseInt(id)).build());
            if (endpoint != null) {
                // 回收旧的DockerClient连接池
                DockerClient dockerClient = getDockerClient();
                if (dockerClient != null) {
                    DockerConnectorHelper.returnDockerClient(endpoint, dockerClient);
                } else {
                    setDockerClient(DockerConnectorHelper.borrowDockerClient(endpoint));
                }
                setEndpoint(endpoint);
            } else {
                log.error(String.format("找不到该ID为【%s】的服务终端，无法切换服务终端", id));
            }
        }
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "切换服务终端成功");
    }
}
