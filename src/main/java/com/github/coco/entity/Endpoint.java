package com.github.coco.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author Yan
 */
@Data
@Builder
public class Endpoint {
    private String id;
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
}
