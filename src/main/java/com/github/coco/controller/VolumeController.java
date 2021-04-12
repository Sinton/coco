package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.DbConstant;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.DockerFilterHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Volume;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping(value = "/api/volume")
public class VolumeController extends BaseController {

    @WebLog
    @PostMapping(value = "/create")
    public Map<String, Object> createVolume(@RequestBody Map<String, Object> params) {
        String volumeName = Objects.toString(params.get("name"), null);
        String driver = Objects.toString(params.get("driver"), null);
        Map<String, String> driverOpts = JSON.parseObject(JSON.toJSONString(params.getOrDefault("driverOpts", "")),
                                                          new TypeReference<Map<String, String>>() {});
        try {
            Volume.Builder volumeBuilder = Volume.builder()
                                                 .name(volumeName)
                                                 .driver(driver);
            if (driverOpts != null && !driverOpts.isEmpty()) {
                volumeBuilder.driverOpts(driverOpts);
            }
            getDockerClient().createVolume(volumeBuilder.build());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "创建存储卷成功");
        } catch (Exception e) {
            log.error("创建容器挂载卷失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/remove")
    public Map<String, Object> removeVolume(@RequestBody Map<String, Object> params) {
        String volumeName = Objects.toString(params.get("volumeName"), null);
        try {
            getDockerClient().removeVolume(volumeName);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除容器挂载卷成功");
        } catch (Exception e) {
            log.error("删除容器挂载卷失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageVolumes(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(Objects.toString(params.getOrDefault("pageNo", DbConstant.PAGE_NO)));
        int pageSize = Integer.parseInt(Objects.toString(params.getOrDefault("pageSize", DbConstant.PAGE_SIZE)));
        List<DockerClient.ListVolumesParam> filters = new ArrayList<>();
        if (params.get(DockerFilterHelper.FILTER_KEY) != null) {
            String filter = JSON.toJSONString(params.get(DockerFilterHelper.FILTER_KEY));
            filters = DockerFilterHelper.getVolumesFilter(filter);
        }
        try {
            List<Volume> volumes = getDockerClient().listVolumes(filters.toArray(new DockerClient.ListVolumesParam[]{}))
                                                    .volumes()
                                                    .stream()
                                                    .sorted((x, y) -> y.createdAt().compareTo(x.createdAt()))
                                                    .collect(Collectors.toList());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, volumes));
        } catch (Exception e) {
            log.error("获取容器挂载卷列表失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> getVolume(@RequestBody Map<String, Object> params) {
        String volumeName = Objects.toString(params.get("volumeName"), null);
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               getDockerClient().inspectVolume(volumeName));
        } catch (Exception e) {
            log.error("获取容器挂载卷失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
