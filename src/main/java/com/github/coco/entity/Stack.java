package com.github.coco.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Stack {
    private String id;
    private String name;
    private String type;
    private String endpointId;
    private String swarmId;
    private String entryPoint;
    private String env;
    private String projectPath;
}
