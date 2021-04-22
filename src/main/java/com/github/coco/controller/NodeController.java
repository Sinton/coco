package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.StringHelper;
import com.spotify.docker.client.messages.swarm.Node;
import com.spotify.docker.client.messages.swarm.NodeSpec;
import com.spotify.docker.client.messages.swarm.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Yan
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/node")
public class NodeController extends BaseController {

    @WebLog
    @PostMapping(value = "/delete")
    public Map<String, Object> deleteNode(@RequestBody Map<String, Object> params) {
        String nodeId = params.getOrDefault("nodeId", null).toString();
        try {
            getDockerClient().deleteNode(nodeId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除集群节点成功");
        } catch (Exception e) {
            log.error("删除集群节点失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/update")
    public Map<String, Object> updateNode(@RequestBody Map<String, Object> params) {
        String nodeId              = Objects.toString(params.get("nodeId"), null);
        String role                = Objects.toString(params.get("role"), "");
        String availability        = Objects.toString(params.get("availability"), "");
        Long version               = Long.parseLong(Objects.toString(params.get("version"), "0"));
        Map<String, String> labels = StringHelper.stringConvertMap(JSON.toJSONString(params.getOrDefault("labels", null)));
        try {
            NodeSpec.Builder nodeSpecBuilder = NodeSpec.builder();
            if (StringUtils.isNotBlank(availability)) {
                nodeSpecBuilder.availability(availability);
            }
            if (StringUtils.isNotBlank(role)) {
                nodeSpecBuilder.role(role);
            }
            if (labels != null && !labels.isEmpty()) {
                nodeSpecBuilder.labels(labels);
            }
            getDockerClient().updateNode(nodeId, version, nodeSpecBuilder.build());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新集群节点成功");
        } catch (Exception e) {
            log.error("更新集群节点节点失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageNodes(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        try {
            List<Node> nodes = getDockerClient().listNodes();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, nodes));
        } catch (Exception e) {
            log.error("获取集群节点列表失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> getNode(@RequestBody Map<String, Object> params) {
        String nodeId = params.getOrDefault("nodeId", null).toString();
        try {
            Map<String, Object> inspect = new HashMap<>(2);
            inspect.put("node", getDockerClient().inspectNode(nodeId));
            List<Task> tasks = getDockerClient().listTasks()
                                                .stream()
                                                .filter(item -> nodeId.equals(item.nodeId()))
                                                .collect(Collectors.toList());
            inspect.put("task", tasks);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, inspect);
        } catch (Exception e) {
            log.error("获取集群节点信息失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
