package com.github.coco.factory;

import lombok.Builder;
import lombok.Data;

/**
 * @author Yan
 */
@Data
@Builder
public class ConnectorConfig {
    private String host;
    private int port;
    private String protocol;
}
