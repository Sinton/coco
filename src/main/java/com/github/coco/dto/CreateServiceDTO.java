package com.github.coco.dto;

import com.spotify.docker.client.messages.swarm.RestartPolicy;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Yan
 */
@Data
public class CreateServiceDTO {
    private String name;
    private String image;
    private String command;
    private Boolean tty;
    private String entrypoint;
    private String workingDir;
    private String user;
    private Character schedulingMode;
    private Integer replicas;
    private List<Map<String, String>> ports;
    private List<Map<String, String>> volumes;
    private List<Map<String, Object>> networkingConfigs;
    private Map<String, String> serviceLabels;
    private Map<String, String> containerLabels;
    private RestartPolicy restartPolicy;
    private Map<String, String> updateConfigPolicy;
    private Map<String, String> resources;
    private List<Map<String, String>> configs;
    private List<Map<String, String>> secrets;
}
