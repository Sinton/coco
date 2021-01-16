package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.Stack;
import com.github.coco.schedule.SyncStackTask;
import com.github.coco.service.StackService;
import com.github.coco.utils.DockerStackHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yan
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/stack")
public class StackController extends BaseController {
    @Resource
    private StackService stackService;

    @Resource
    private SyncStackTask syncStackTask;

    @WebLog
    @PostMapping("/create")
    public Map<String, Object> createStack(@RequestBody Map<String, Object> params) {
        String namespace = Objects.toString(params.get("namespace"), "");
        DockerStackHelper.deployStack(getDockerClient(), namespace);
        return null;
    }

    @WebLog
    @PostMapping("/remove")
    public Map<String, Object> removeStack(@RequestBody Map<String, Object> params) {
        String namespace = Objects.toString(params.get("namespace"), "");
        DockerStackHelper.removeStack(getDockerClient(), namespace);
        return null;
    }

    @WebLog
    @PostMapping(value = "/update")
    public Map<String, Object> updateStack(@RequestBody Map<String, Object> params) {
        String namespace = Objects.toString(params.get("namespace"), "");
        DockerStackHelper.deployStack(getDockerClient(), namespace);
        return null;
    }

    @WebLog
    @PostMapping("/list")
    public Map<String, Object> getPageStacks(@RequestBody Map<String, Object> params) {
        syncStackTask.syncStacks();

        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        try {
            List<Stack> stacks = stackService.getStacks(getDockerClient().getHost());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, stacks));
        } catch (Exception e) {
            log.error("获取应用栈列表失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
