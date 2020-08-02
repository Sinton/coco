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
    private String ip;
    private Integer port;
    private String url;
    private String tls;
}
