package com.github.coco.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yan
 */
@Data
public class Pipline implements Serializable {
    private String id;
    private String name;
    private Character type;
    private Character status;
    private Long createTime;
}
