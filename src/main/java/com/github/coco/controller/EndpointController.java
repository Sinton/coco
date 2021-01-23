package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.DbConstant;
import com.github.coco.constant.ErrorConstant;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.EndpointTypeEnum;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.Endpoint;
import com.github.coco.schedule.SyncEndpointTask;
import com.github.coco.service.EndpointService;
import com.github.coco.utils.DockerConnectorHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        String publicIp = (String) params.get("ip");
        String endpointUrl = (String) params.get("url");
        int port = DockerHost.defaultPort();
        if (endpointUrl.contains(GlobalConstant.SPACEMARK_COLON)) {
            port = Integer.parseInt(endpointUrl.substring(endpointUrl.indexOf(GlobalConstant.SPACEMARK_COLON) + GlobalConstant.SPACEMARK_COLON.length()));
        }
        // 检查是否已存在
        Endpoint endpoint = endpointService.getEndpoint(Endpoint.builder().publicIp(publicIp).build());
        if (endpoint == null) {
            endpoint = Endpoint.builder()
                               .name(name)
                               .publicIp(publicIp)
                               .port(port)
                               .endpointUrl(endpointUrl)
                               .endpointType(EndpointTypeEnum.URL.getCode())
                               .build();
            try {
                endpointService.createEndpoint(endpoint);
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
            boolean notChangedEndpointUrl = endpointService.getEndpoint(Endpoint.builder().id(endpoint.getId()).build())
                                                           .getEndpointUrl()
                                                           .equals(endpoint.getEndpointUrl());
            if (!notChangedEndpointUrl) {
                // 切换释放对象连接池中服务终端对应的DockerClient
                DockerClient dockerClient = getDockerClient();
                setDockerClient(DockerConnectorHelper.borrowDockerClient(endpoint));
                DockerConnectorHelper.returnDockerClient(endpoint, dockerClient);
            }
            endpointService.modifyEndpoint(endpoint);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "修改服务终端成功");
        } catch (Exception e) {
            log.error("修改服务终端失败", e);
            return apiResponseDTO.returnResult(ErrorConstant.ERR_BASE_COMMON, "修改服务终端失败");
        }
    }

    @WebLog
    @PostMapping("/list")
    public Map<String, Object> getPageEndpoints(@RequestBody Map<String, Object> params) {
        syncEndpointTask.syncEndpoints();

        int pageNo = Integer.parseInt(String.valueOf(params.getOrDefault("pageNo", DbConstant.PAGE_NO)));
        int pageSize = Integer.parseInt(String.valueOf(params.getOrDefault("pageSize", DbConstant.PAGE_SIZE)));
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
                setDockerClient(DockerConnectorHelper.borrowDockerClient(endpoint));
            } else {
                log.info("找不到该服务终端，无法切换服务终端");
            }
        }
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, null);
    }
}
