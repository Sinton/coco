package com.github.coco.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yan
 */
@Data
public class Endpoint implements Serializable {
    private String ip;
    private Integer port;
    private String username;
    private Character authType;
    private String password;
}
