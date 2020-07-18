package com.github.coco.terminal;

import lombok.Data;

/**
 * @author Yan
 */
@Data
public class WebTerminalUser {
    private String operate;
    private String host;
    private Integer port = 22;
    private String username;
    private String password;
    private String command = "";
}
