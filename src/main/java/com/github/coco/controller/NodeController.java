package com.github.coco.controller;

import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.messages.swarm.Node;
import com.spotify.docker.client.messages.swarm.Task;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/node")
public class NodeController extends BaseController {

    @PostMapping(value = "/delete")
    public Map<String, Object> deleteNode(@RequestBody Map<String, Object> params) {
        String nodeId = params.getOrDefault("nodeId", null).toString();
        try {
            dockerClient.deleteNode(nodeId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除集群成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(this.getClass(), e, "删除集群节点");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @PostMapping(value = "/list")
    public Map<String, Object> getPageNodes(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        try {
            List<Node> nodes = dockerClient.listNodes();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, nodes));
        } catch (Exception e) {
            LoggerHelper.fmtError(this.getClass(), e, "获取集群节点列表失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @PostMapping(value = "/inspect")
    public Map<String, Object> getNode(@RequestBody Map<String, Object> params) {
        String nodeId = params.getOrDefault("nodeId", null).toString();
        try {
            Map<String, Object> inspect = new HashMap<>(2);
            inspect.put("node", dockerClient.inspectNode(nodeId));
            List<Task> tasks = dockerClient.listTasks()
                                           .stream()
                                           .filter(item -> nodeId.equals(item.nodeId()))
                                           .collect(Collectors.toList());
            inspect.put("task", tasks);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, inspect);
        } catch (Exception e) {
            LoggerHelper.fmtError(this.getClass(), e, "获取集群节点信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
