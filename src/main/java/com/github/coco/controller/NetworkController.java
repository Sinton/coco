package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.DockerFilterHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Ipam;
import com.spotify.docker.client.messages.IpamConfig;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.NetworkConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * @author Yan
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/network")
public class NetworkController extends BaseController {
    @Data
    public static class NetworkConf {
        private String subnet;
        private String ipRange;
        private String gateway;
        private Boolean attachable;
        private Boolean internal;
    }

    @WebLog
    @PostMapping(value = "/create")
    public Map<String, Object> createNetwork(@RequestBody Map<String, Object> params) {
        String networkName = String.valueOf(params.getOrDefault("name", ""));
        String driver = String.valueOf(params.getOrDefault("driver", ""));
        Map<String, String> labels = JSON.parseObject(JSON.toJSONString(params.getOrDefault("labels", "{}")),
                                                      new TypeReference<Map<String, String>>() {});
        Map<String, String> driverOpts = JSON.parseObject(JSON.toJSONString(params.getOrDefault("driverOpts", "{}")),
                                                          new TypeReference<Map<String, String>>() {});
        NetworkConf networkConfig = JSON.parseObject(JSON.toJSONString(params.getOrDefault("networkConfig", "{}")),
                                                     NetworkConf.class);
        Boolean attachable = networkConfig.getAttachable();
        Boolean internal = networkConfig.getInternal();
        try {
            NetworkConfig.Builder networkConfigBuilder = NetworkConfig.builder();
            networkConfigBuilder.name(networkName);
            networkConfigBuilder.driver(driver);
            if (StringUtils.isNotBlank(networkConfig.getSubnet()) &&
                StringUtils.isNotBlank(networkConfig.getIpRange()) &&
                StringUtils.isNotBlank(networkConfig.getGateway())) {
                IpamConfig ipamConfig = IpamConfig.create(networkConfig.getSubnet(),
                                                          networkConfig.getIpRange(),
                                                          networkConfig.getGateway());
                Ipam ipam = Ipam.builder().driver(driver)
                                          .config(singletonList(ipamConfig))
                                          .build();
                networkConfigBuilder.ipam(ipam);
            }
            if (labels != null && !labels.isEmpty()) {
                networkConfigBuilder.labels(labels);
            }
            if (driverOpts != null && !driverOpts.isEmpty()) {
                networkConfigBuilder.options(driverOpts);
            }
            networkConfigBuilder.attachable(attachable);
            networkConfigBuilder.internal(internal);
            networkConfigBuilder.checkDuplicate(true);
            getDockerClient().createNetwork(networkConfigBuilder.build());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "创建容器网络成功");
        } catch (DockerException | InterruptedException e) {
            log.error("创建容器网络失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "创建容器网络失败");
        }
    }

    @WebLog
    @PostMapping(value = "/remove")
    public Map<String, Object> removeNetwork(@RequestBody Map<String, Object> params) {
        String networkId = String.valueOf(params.getOrDefault("networkId", ""));
        try {
            getDockerClient().removeNetwork(networkId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除容器网络成功");
        } catch (DockerException | InterruptedException e) {
            log.error("删除容器网络失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "删除容器网络失败");
        }
    }

    @WebLog
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
            List<Network> networks = getDockerClient().listNetworks(DockerFilterHelper.toArray(filters,
                                                                                               DockerClient.ListNetworksParam.class))
                                                  .stream()
                                                  .sorted((x, y) -> y.created().compareTo(x.created()))
                                                  .collect(Collectors.toList());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, networks));
        } catch (DockerException | InterruptedException e) {
            log.error("获取容器网络列表失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> getNetwork(@RequestBody Map<String, Object> params) {
        String networkId = params.getOrDefault("networkId", null).toString();
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               getDockerClient().inspectNetwork(networkId));
        } catch (DockerException | InterruptedException e) {
            log.error("获取容器网络信息失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/join")
    public Map<String, Object> connectNetwork(@RequestBody Map<String, Object> params) {
        String containerId = params.getOrDefault("containerId", null).toString();
        String networkId = params.getOrDefault("networkId", null).toString();
        try {
            getDockerClient().connectToNetwork(containerId, networkId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "加入网络成功");
        } catch (DockerException | InterruptedException e) {
            log.error("加入网络失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/leave")
    public Map<String, Object> disconnectNetwork(@RequestBody Map<String, Object> params) {
        String containerId = params.getOrDefault("containerId", null).toString();
        String networkId = params.getOrDefault("networkId", null).toString();
        boolean force = Objects.nonNull(params.get("force")) && Boolean.parseBoolean(params.get("force").toString());
        try {
            getDockerClient().disconnectFromNetwork(containerId, networkId, force);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "离开网络成功");
        } catch (DockerException | InterruptedException e) {
            log.error("离开网络失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
