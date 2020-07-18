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
    private String host;
    private Integer port;
    private String user;
    private String password;
}
