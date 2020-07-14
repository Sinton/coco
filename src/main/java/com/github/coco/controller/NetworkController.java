package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.DockerFilterHelper;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Network;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/network")
public class NetworkController extends BaseController {

    @PostMapping(value = "/remove")
    public Map<String, Object> removeNetwork(@RequestBody Map<String, Object> params) {
        String networkId = params.getOrDefault("networkId", null).toString();
        try {
            dockerClient.removeNetwork(networkId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除容器网络成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(this.getClass(), e, "删除容器网络失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "删除容器网络失败");
        }
    }

    @PostMapping(value = "/list")
    public Map<String, Object> getPageNetworks(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        List<DockerClient.ListNetworksParam> filters = new ArrayList<>();
        if (params.get(DockerFilterHelper.FILTER_KEY) != null) {
            String filter = JSON.toJSONString(params.get(DockerFilterHelper.FILTER_KEY));
            filters = DockerFilterHelper.getNetworkFilter(filter);
        }
        try {
            List<Network> networks = dockerClient.listNetworks(DockerFilterHelper.toArray(filters,
                                                                                          DockerClient.ListNetworksParam.class))
                                                  .stream()
                                                  .sorted((x, y) -> y.created().compareTo(x.created()))
                                                  .collect(Collectors.toList());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, networks));
        } catch (Exception e) {
            LoggerHelper.fmtError(this.getClass(), e, "获取容器网络列表失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @PostMapping(value = "/inspect")
    public Map<String, Object> getNetwork(@RequestBody Map<String, Object> params) {
        String networkId = params.getOrDefault("networkId", null).toString();
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.inspectNetwork(networkId));
        } catch (Exception e) {
            LoggerHelper.fmtError(this.getClass(), e, "获取容器网络信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @PostMapping(value = "/join")
    public Map<String, Object> connectNetwork(@RequestBody Map<String, Object> params) {
        String containerId = params.getOrDefault("containerId", null).toString();
        String networkId = params.getOrDefault("networkId", null).toString();
        try {
            dockerClient.connectToNetwork(containerId, networkId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "加入网络成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(this.getClass(), e, "加入网络失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @PostMapping(value = "/leave")
    public Map<String, Object> disconnectNetwork(@RequestBody Map<String, Object> params) {
        String containerId = params.getOrDefault("containerId", null).toString();
        String networkId = params.getOrDefault("networkId", null).toString();
        boolean force = Objects.nonNull(params.get("force")) && Boolean.parseBoolean(params.get("force").toString());
        try {
            dockerClient.disconnectFromNetwork(containerId, networkId, force);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "离开网络成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(this.getClass(), e, "离开网络失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
