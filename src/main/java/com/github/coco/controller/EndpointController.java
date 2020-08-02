package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.entity.Endpoint;
import com.github.coco.service.EndpointService;
import com.github.coco.utils.DockerConnectorHelper;
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
@RestController
@RequestMapping(value = "/api/endpoint")
public class EndpointController extends BaseController {
    @Resource
    private EndpointService endpointService;

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
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                           apiResponseDTO.tableResult(pageNo, pageSize, endpointService.getEndpoints()));
    }

    @WebLog
    @PostMapping("/switch")
    public Map<String, Object> switchEndpoint(@RequestBody Map<String, Object> params) {
        String id = Objects.toString(params.get("id"), "");
        if (StringUtils.isNotBlank(id)) {
            Endpoint endpoint = endpointService.getEndpointById(id);
            dockerClient = DockerConnectorHelper.getDockerClient(endpoint.getIp(), endpoint.getPort());
        }
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, null);
    }

    @WebLog
    @PostMapping("/health")
    public Map<String, Object> healthCheckEndpoint(@RequestBody Map<String, Object> params) {
        return null;
    }
}
