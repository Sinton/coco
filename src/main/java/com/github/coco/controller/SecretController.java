package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.messages.swarm.Secret;
import com.spotify.docker.client.messages.swarm.SecretSpec;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/secret")
public class SecretController extends BaseController {

    @WebLog
    @PostMapping(value = "/create")
    public Map<String, Object> createSecret(@RequestBody Map<String, Object> params) {
        String name = params.getOrDefault("name", null).toString();
        String data = params.getOrDefault("data", null).toString();
        try {
            SecretSpec secretSpec = SecretSpec.builder()
                                              .name(name)
                                              .data(data)
                                              .labels(null)
                                              .build();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.createSecret(secretSpec));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "创建加密配置项失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/remove")
    public Map<String, Object> removeSecret(@RequestBody Map<String, Object> params) {
        String secretId = params.getOrDefault("secretId", null).toString();
        try {
            dockerClient.deleteSecret(secretId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除加密配置项成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "删除加密配置项失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageSecrets(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        try {
            List<Secret> secrets = dockerClient.listSecrets();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, secrets));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取加密配置项列表失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> getSecret(@RequestBody Map<String, Object> params) {
        String secretId = params.getOrDefault("secretId", null).toString();
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.inspectSecret(secretId));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), "获取加密配置项信息失败");
            return  apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
