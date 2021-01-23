package com.github.coco.schedule;

import com.github.coco.constant.DbConstant;
import com.github.coco.constant.DockerConstant;
import com.github.coco.constant.dict.StackTypeEnum;
import com.github.coco.entity.Stack;
import com.github.coco.service.EndpointService;
import com.github.coco.service.StackService;
import com.github.coco.utils.DockerConnectorHelper;
import com.github.coco.utils.ListHelper;
import com.spotify.docker.client.DockerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Yan
 */
@Slf4j
@Component
public class SyncStackTask {
    @Resource
    private EndpointService endpointService;

    @Resource
    private StackService stackService;

    public void syncStacks() {
        List<String> swarmStacks = new ArrayList<>();
        List<String> composeStacks = new ArrayList<>();
        List<String> dbSwarmStacks = new ArrayList<>();
        List<String> dbComposeStacks = new ArrayList<>();
        endpointService.getEndpoints(DbConstant.PAGE_NO, DbConstant.PAGE_SIZE).forEach(endpoint -> {
            DockerClient dockerClient = DockerConnectorHelper.borrowDockerClient(endpoint);
            try {
                if (dockerClient != null) {
                    // 通过容器的标签筛选docker-compose应用栈
                    dockerClient.listContainers(DockerClient.ListContainersParam.allContainers()).forEach(container -> {
                        if (container.labels() != null && !container.labels().isEmpty()) {
                            String stackName;
                            if (container.labels().containsKey(DockerConstant.SWARM_STACK_LABEL)) {
                                stackName = container.labels().get(DockerConstant.SWARM_STACK_LABEL);
                                if (!swarmStacks.contains(stackName)) {
                                    swarmStacks.add(stackName);
                                }
                            } else if (container.labels().containsKey(DockerConstant.COMPOSE_STACK_LABEL)) {
                                stackName = container.labels().get(DockerConstant.COMPOSE_STACK_LABEL);
                                if (!composeStacks.contains(stackName)) {
                                    composeStacks.add(stackName);
                                }
                            }
                        }
                    });

                    // 是否是Swarm集群
                    if (dockerClient.info().swarm() != null && dockerClient.info().swarm().controlAvailable()) {
                        dockerClient.listServices().forEach(service -> {
                            if (service.spec().labels() != null && !service.spec().labels().isEmpty()) {
                                String stackName;
                                if (service.spec().labels().containsKey(DockerConstant.SWARM_STACK_LABEL)) {
                                    stackName = service.spec().labels().get(DockerConstant.SWARM_STACK_LABEL);
                                    if (!swarmStacks.contains(stackName)) {
                                        swarmStacks.add(stackName);
                                    }
                                } else if (service.spec().labels().containsKey(DockerConstant.COMPOSE_STACK_LABEL)) {
                                    stackName = service.spec().labels().get(DockerConstant.COMPOSE_STACK_LABEL);
                                    if (!composeStacks.contains(stackName)) {
                                        composeStacks.add(stackName);
                                    }
                                }
                            }
                        });
                    }
                }

                dbSwarmStacks.addAll(stackService.getStacks(endpoint.getPublicIp())
                                                 .stream()
                                                 .filter(item -> item.getType().equals(StackTypeEnum.SWARM.getCode()) &&
                                                                 item.getEndpoint().equals(endpoint.getPublicIp()))
                                                 .map(Stack::getName)
                                                 .collect(Collectors.toList()));
                dbComposeStacks.addAll(stackService.getStacks(endpoint.getPublicIp())
                                                   .stream()
                                                   .filter(item -> item.getType().equals(StackTypeEnum.COMPOSE.getCode()) &&
                                                                   item.getEndpoint().equals(endpoint.getPublicIp()))
                                                   .map(Stack::getName)
                                                   .collect(Collectors.toList()));
            } catch (Exception e) {
                log.error("获取应用栈信息异常", e);
            } finally {
                DockerConnectorHelper.returnDockerClient(endpoint, dockerClient);
            }

            // 更新调整当前服务终端的应用栈信息
            try {
                // Swarm 集群应用栈
                adjustDbStacks(dbSwarmStacks, swarmStacks, StackTypeEnum.SWARM, endpoint.getPublicIp());
                // docker-compose 应用栈
                adjustDbStacks(dbComposeStacks, composeStacks, StackTypeEnum.COMPOSE, endpoint.getPublicIp());
            } catch (Exception e) {
                log.error("更新应用栈信息异常", e);
            }
        });
    }

    /**
     * 调整数据库中的应用栈信息
     *
     * @param dbStacks
     * @param stacks
     * @param stackType
     * @param endpoint
     */
    private void adjustDbStacks(List<String> dbStacks, List<String> stacks, StackTypeEnum stackType, String endpoint) {
        Stack.StackBuilder builder = Stack.builder()
                                          .type(stackType.getCode())
                                          .endpoint(endpoint);

        ListHelper.getRightJoinList(dbStacks, stacks).forEach(stackName -> {
            // 要新增的应用栈
            if (stackService.getStack(builder.name(stackName).build()) == null) {
                stackService.createStack(builder.id(UUID.randomUUID().toString()).name(stackName).build());
            }
        });

        ListHelper.getLeftJoinList(dbStacks, stacks).forEach(stackName -> {
            // 要删除的应用栈
            stackService.removeStack(builder.name(stackName).build());
        });

        ListHelper.getInnerJoinList(dbStacks, stacks).forEach(stackName -> {
            // 要更新的应用栈
            stackService.modifyStack(builder.name(stackName).build());
        });
    }
}
