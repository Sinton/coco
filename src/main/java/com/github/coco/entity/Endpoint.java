package com.github.coco.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Yan
 */
@Data
@Builder
@AllArgsConstructor
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
}
