package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.DbConstant;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Config;
import com.spotify.docker.client.messages.swarm.ConfigSpec;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping(value = "/api/config")
public class ConfigController extends BaseController {

    @WebLog
    @PostMapping(value = "/create")
    public Map<String, Object> createConfig(@RequestBody Map<String, Object> params) {
        String name = Objects.toString(params.get("name"), "");
        String data = Objects.toString(params.get("data"), "");
        Map<String, String> labels = JSON.parseObject(JSON.toJSONString(params.get("labels")),
                                                      new TypeReference<Map<String, String>>() {});
        try {
            String encodedData = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
            ConfigSpec.Builder configSpecBuilder = ConfigSpec.builder()
                                                             .name(name)
                                                             .data(encodedData);
            if (!labels.isEmpty()) {
                configSpecBuilder.labels(labels);
            }
            getDockerClient().createConfig(configSpecBuilder.build());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "创建Swarm配置项成功");
        } catch (DockerException | InterruptedException e) {
            log.error("创建Swarm配置项失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/clone")
    public Map<String, Object> cloneConfig(@RequestBody Map<String, Object> params) {
        String configId = Objects.toString(params.get("configId"), "");
        String name     = Objects.toString(params.get("name"), "");
        try {
            ConfigSpec configSpec = getDockerClient().inspectConfig(configId).configSpec();
            ConfigSpec cloneConfigSpec = ConfigSpec.builder()
                                                   .name(name)
                                                   .data(configSpec.data())
                                                   .labels(configSpec.labels())
                                                   .build();
            getDockerClient().createConfig(cloneConfigSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "克隆Swarm配置项成功");
        } catch (DockerException | InterruptedException e) {
            log.error("克隆Swarm配置项失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/remove")
    public Map<String, Object> removeConfig(@RequestBody Map<String, Object> params) {
        String configId = Objects.toString(params.get("configId"), "");
        try {
            getDockerClient().deleteConfig(configId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除Swarm配置项成功");
        } catch (DockerException | InterruptedException e) {
            log.error("删除Swarm配置项失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/update")
    public Map<String, Object> updateConfig(@RequestBody Map<String, Object> params) {
        String configId            = Objects.toString(params.get("configId"), "");
        String data                = Objects.toString(params.get("data"), "");
        Map<String, String> labels = JSON.parseObject(Objects.toString(params.get("labels"), "{}"),
                                                      new TypeReference<Map<String, String>>() {});
        try {
            Config config = getDockerClient().inspectConfig(configId);
            ConfigSpec configSpec = ConfigSpec.builder()
                                              .name(config.configSpec().name())
                                              .labels(labels)
                                              .build();
            getDockerClient().updateConfig(config.id(), config.version().index(), configSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "修改Swarm配置项成功");
        } catch (DockerException | InterruptedException e) {
            log.error("修改Swarm配置项失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageConfigs(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(Objects.toString(params.getOrDefault("pageNo", DbConstant.PAGE_NO)));
        int pageSize = Integer.parseInt(Objects.toString(params.getOrDefault("pageSize", DbConstant.PAGE_SIZE)));
        try {
            List<Config> configs = getDockerClient().listConfigs();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, configs));
        } catch (DockerException | InterruptedException e) {
            log.error("获取Swarm配置项列表失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> inspectConfig(@RequestBody Map<String, Object> params) {
        String configId = Objects.toString(params.get("configId"), "");
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               getDockerClient().inspectConfig(configId));
        } catch (DockerException | InterruptedException e) {
            log.error("获取Swarm配置项摘要信息失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
