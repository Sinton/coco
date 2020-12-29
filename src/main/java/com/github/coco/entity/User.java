package com.github.coco.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yan
 */
@Data
public class User implements Serializable {
    private Integer uid;
    private String username;
    private String salt;
    private String password;
    private String nickname;
    private String avatar;
    private Integer status;
    private String email;
    private String telephone;
    private String lang;
    private String lastLoginIp;
    private Long lastLoginTime;
    private Integer creatorId;
    private Long createTime;
}
