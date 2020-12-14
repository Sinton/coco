package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.Stack;
import com.github.coco.schedule.SyncStackTask;
import com.github.coco.service.StackService;
import com.github.coco.utils.LoggerHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/stack")
public class StackController extends BaseController {
    @Resource
    private StackService stackService;

    @Resource
    private SyncStackTask syncStackTask;

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
            LoggerHelper.fmtError(getClass(), e, "获取应用栈列表失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
