package com.github.coco.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author Yan
 */
@Data
@Builder
public class Stack {
    private String id;
    private String name;
    private Integer status;
    private Integer type;
    private String endpoint;
    private String swarmId;
    private String projectPath;
}
