package com.github.coco.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author Yan
 */
@Data
@Builder
public class Stack {
    private Integer id;
    private String name;
    private Integer status;
    private Integer type;
    private String endpoint;
    private String projectPath;
    private Integer owner;
    private Integer internal;
}
