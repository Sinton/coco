package com.github.coco.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author Yan
 */
@Data
@Builder
public class Host {
    private String id;
    private String ip;
    private Integer port;
    private String user;
    private String password;
    private String hostname;
    private Character dockerized;
    private String resources;
}
