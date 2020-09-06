package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.messages.swarm.Config;
import com.spotify.docker.client.messages.swarm.ConfigSpec;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/config")
public class ConfigController extends BaseController {

    @WebLog
    @PostMapping(value = "/create")
    public Map<String, Object> createConfig(@RequestBody Map<String, Object> params) {
        String name = params.getOrDefault("name", null).toString();
        String data = params.getOrDefault("data", null).toString();
        Map<String, String> labels = JSON.parseObject(Objects.toString(params.get("labels"), ""),
                                                      new TypeReference<Map<String, String>>() {});
        try {
            ConfigSpec configSpec = ConfigSpec.builder()
                                              .name(name)
                                              .data(Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8)))
                                              .labels(labels.isEmpty() ? null : labels)
                                              .build();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.createConfig(configSpec));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "创建容器配置项失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/clone")
    public Map<String, Object> cloneConfig(@RequestBody Map<String, Object> params) {
        String configId = Objects.toString(params.get("configId"), "");
        String name     = Objects.toString(params.get("name"), "");
        try {
            ConfigSpec configSpec = dockerClient.inspectConfig(configId).configSpec();
            ConfigSpec cloneConfigSpec = ConfigSpec.builder()
                                                   .name(name)
                                                   .data(configSpec.data())
                                                   .labels(configSpec.labels())
                                                   .build();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.createConfig(cloneConfigSpec));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "克隆容器配置项失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/remove")
    public Map<String, Object> removeConfig(@RequestBody Map<String, Object> params) {
        String configId = Objects.toString(params.get("configId"), "");
        try {
            dockerClient.deleteConfig(configId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除配置项成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "删除容器配置项失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/update")
    public Map<String, Object> updateConfig(@RequestBody Map<String, Object> params) {
        String configId            = Objects.toString(params.get("configId"), "");
        String data                = Objects.toString(params.get("data"), "");
        Map<String, String> labels = JSON.parseObject(Objects.toString(params.get("data"), ""),
                                                      new TypeReference<Map<String, String>>() {});
        try {
            Config config = dockerClient.inspectConfig(configId);
            ConfigSpec configSpec = ConfigSpec.builder()
                                              .name(config.configSpec().name())
                                              .labels(labels)
                                              .build();
            dockerClient.updateConfig(config.id(), config.version().index(), configSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "修改配置项成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "修改容器配置项失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageConfigs(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        try {
            List<Config> configs = dockerClient.listConfigs();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, configs));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取容器配置项列表失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> inspectConfig(@RequestBody Map<String, Object> params) {
        String configId = Objects.toString(params.get("configId"), "");
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.inspectConfig(configId));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取容器配置项摘要信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
