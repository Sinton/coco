package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.ErrorConstant;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.EndpointTypeEnum;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.Endpoint;
import com.github.coco.schedule.SyncEndpointTask;
import com.github.coco.service.EndpointService;
import com.github.coco.utils.DockerConnectorHelper;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.DockerHost;
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
                LoggerHelper.fmtError(getClass(), e, "创建终端服务发生异常");
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
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除服务重点成功");
        } catch (Exception e) {
            return apiResponseDTO.returnResult(ErrorConstant.ERR_BASE_COMMON, "删除服务重点失败");
        }
    }

    @WebLog
    @PostMapping("/list")
    public Map<String, Object> getPageEndpoints(@RequestBody Map<String, Object> params) {
        syncEndpointTask.syncEndpoints();

        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                           apiResponseDTO.tableResult(1, 10, endpointService.getEndpoints(pageNo, pageSize)));
    }

    @WebLog
    @PostMapping("/switch")
    public Map<String, Object> switchEndpoint(@RequestBody Map<String, Object> params) {
        String id = Objects.toString(params.get("id"), "");
        if (StringUtils.isNotBlank(id)) {
            Endpoint endpoint = endpointService.getEndpoint(Endpoint.builder().id(Integer.parseInt(id)).build());
            if (endpoint != null) {
                dockerClient = DockerConnectorHelper.borrowDockerClient(endpoint);
            } else {
                LoggerHelper.fmtInfo(getClass(),"找不到该服务终端，无法切换服务终端");
            }
        }
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, null);
    }
}
