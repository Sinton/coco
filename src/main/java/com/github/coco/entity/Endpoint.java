package com.github.coco.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author Yan
 */
@Data
@Builder
public class Endpoint {
    private Integer id;
    private String name;
    private String publicIp;
    private Integer port;
    private String endpointUrl;
    private Integer endpointType;
    private Integer status;
    private String resources;
    private String dockerConfig;
    private Integer tlsEnable;
    private String tlsConfig;
    private Long updateDateTime;
    private Integer owner;

    public Endpoint(Integer id, String name, String publicIp, Integer port, String endpointUrl, Integer endpointType, Integer status, String resources, String dockerConfig, Integer tlsEnable, String tlsConfig, Long updateDateTime, Integer owner) {
        this.id = id;
        this.name = name;
        this.publicIp = publicIp;
        this.port = port;
        this.endpointUrl = endpointUrl;
        this.endpointType = endpointType;
        this.status = status;
        this.resources = resources;
        this.dockerConfig = dockerConfig;
        this.tlsEnable = tlsEnable;
        this.tlsConfig = tlsConfig;
        this.updateDateTime = updateDateTime;
        this.owner = owner;
    }
}
