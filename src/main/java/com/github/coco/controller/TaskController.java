package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.swarm.Task;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/task")
public class TaskController extends BaseController {

    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageTasks(@RequestBody Map<String, Object> params) {
        String serviceId = Objects.toString(params.get("serviceId"), null);
        boolean groupBy = Boolean.parseBoolean(params.getOrDefault("groupBy", false).toString());
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        try {
            List<Task> tasks = getDockerClient().listTasks()
                                                .stream()
                                                .sorted((x, y) -> y.createdAt().compareTo(x.createdAt()))
                                                .sorted((x, y) -> y.updatedAt().compareTo(x.updatedAt()))
                                                .collect(Collectors.toList());
            if (groupBy) {
                tasks = tasks.stream()
                             .filter(item -> item.serviceId().equals(serviceId))
                             .collect(Collectors.toList());
            }
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, tasks));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取调度任务列表失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "获取调度任务失败");
        }
    }

    @WebLog
    @PostMapping(value = "/logs")
    public Map<String, Object> getTaskLog(@RequestBody Map<String, Object> params) {
        String taskId = Objects.toString(params.get("taskId"), null);
        boolean stdErr = Boolean.parseBoolean(params.getOrDefault("stdErr", true).toString());
        boolean stdOut = Boolean.parseBoolean(params.getOrDefault("stdOut", true).toString());
        boolean timestamps = Boolean.parseBoolean(params.getOrDefault("timestamps", false).toString());
        int tail = Integer.parseInt(params.getOrDefault("tail", -1).toString());
        if (taskId != null) {
            try {
                String logs = getDockerClient().taskLogs(taskId,
                                                         DockerClient.LogsParam.stderr(stdErr),
                                                         DockerClient.LogsParam.stdout(stdOut),
                                                         DockerClient.LogsParam.tail(tail),
                                                         DockerClient.LogsParam.timestamps(timestamps),
                                                         DockerClient.LogsParam.follow(true))
                                               .readFully();
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, logs);
            } catch (Exception e) {
                LoggerHelper.fmtError(getClass(), e, "获取调度任务日志");
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
            }
        } else {
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "taskId不能为空");
        }
    }
}
