package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.DockerFilterHelper;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Volume;
import org.apache.commons.lang.StringUtils;
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
@RequestMapping(value = "/api/volume")
public class VolumeController extends BaseController {

    @WebLog
    @PostMapping(value = "/create")
    public Map<String, Object> createVolume(@RequestBody Map<String, Object> params) {
        String volumeName = Objects.toString(params.get("volumeName"), null);
        String driver = Objects.toString(params.get("driver"), null);
        try {
            Volume volume = Volume.builder()
                                  .name(volumeName)
                                  .driver(driver)
                                  .build();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               getDockerClient().createVolume(volume));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "创建容器挂载卷失败");
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
            LoggerHelper.fmtError(getClass(), e, "删除容器挂载卷失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageVolumes(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        String filter = Objects.toString(params.get(DockerFilterHelper.FILTER_KEY), null);
        List<DockerClient.ListVolumesParam> filters = new ArrayList<>();
        if (StringUtils.isNotBlank(filter)) {
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
            LoggerHelper.fmtError(getClass(), e, "获取容器挂载卷列表失败");
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
            LoggerHelper.fmtError(getClass(), e, "获取容器挂载卷失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
