package com.github.coco.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yan
 */
@Data
public class User {
    private Long uid;
    private String nickname;
    private String username;
    private String password;
    private String salt;
    private String email;
    private Long lastLoginTime;
}
