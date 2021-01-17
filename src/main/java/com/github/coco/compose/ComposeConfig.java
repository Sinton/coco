package com.github.coco.compose;

import lombok.Builder;
import lombok.Data;

/**
 * @author Yan
 */
@Data
@Builder
public class ComposeConfig {
    private Integer projectId;
    private String project;
    private String serviceName;
    private String serviceIndex;
    private String command;
    private String moduleName;
    private Boolean pruneVolume;
    private String remote;
    private Boolean daemon;
    private Boolean followLogs;
    private Boolean debug;
}
