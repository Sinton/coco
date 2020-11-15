package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.Endpoint;
import com.github.coco.schedule.SyncEndpointTask;
import com.github.coco.service.EndpointService;
import com.github.coco.utils.DockerConnectorHelper;
import com.github.coco.utils.LoggerHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Yan
 */
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
        String publicIp = (String) params.get("ip");
        // 检查是否已存在
        Endpoint endpoint = endpointService.getEndpoint(Endpoint.builder().publicIp(publicIp).build());
        if (endpoint == null) {
            endpoint = Endpoint.builder()
                               .id(UUID.randomUUID().toString())
                               .publicIp(publicIp)
                               .build();
            endpointService.createEndpoint(endpoint);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, endpointService.getEndpoint(endpoint));
        } else {
            LoggerHelper.fmtError(getClass(), "创建终端服务失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "创建终端服务失败");
        }
    }

    @WebLog
    @PostMapping("/remove")
    public Map<String, Object> removeEndpoint(@RequestBody Map<String, Object> params) {
        String id = (String) params.get("id");
        endpointService.removeEndpoint(id);
        return null;
    }

    @WebLog
    @PostMapping("/list")
    public Map<String, Object> getPageEndpoints(@RequestBody Map<String, Object> params) {
        syncEndpointTask.syncEndpoints();

        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, endpointService.getEndpoints());
    }

    @WebLog
    @PostMapping("/switch")
    public Map<String, Object> switchEndpoint(@RequestBody Map<String, Object> params) {
        String id = Objects.toString(params.get("id"), "");
        if (StringUtils.isNotBlank(id)) {
            Endpoint endpoint = endpointService.getEndpointById(id);
            dockerClient = DockerConnectorHelper.borrowDockerClient(endpoint.getPublicIp(), endpoint.getPort());
        }
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, null);
    }
}
