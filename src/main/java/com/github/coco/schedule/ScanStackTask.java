package com.github.coco.schedule;

import com.github.coco.factory.DockerConnector;
import com.github.coco.utils.DockerConnectorHelper;
import com.spotify.docker.client.DockerClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yan
 */
@Component
public class ScanStackTask {
    private static final DockerClient dockerClient = DockerConnectorHelper.getDockerClient("192.168.3.140", 2375);
    private static final String STACK_LABEL = "com.docker.stack.namespace";

    public void getStack() {
        try {
            List<String> stacks = new ArrayList<>();
            if (dockerClient.info().swarm() != null && dockerClient.info().swarm().controlAvailable()) {
                dockerClient.listServices().forEach(item -> {
                    if (item.spec().labels() != null && item.spec().labels().containsKey(STACK_LABEL)) {
                        stacks.add(item.spec().labels().get(STACK_LABEL));
                    }
                });
            }
            // TODO 如果不存在则在数据库中删除
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 2 * 1000)
    public void get() {
    }
}
