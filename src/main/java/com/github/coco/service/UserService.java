package com.github.coco.service;

import com.github.coco.entity.User;

/**
 * @author Yan
 */
public interface UserService {
    /**
     * 根据用户ID获取用户信息
     *
     * @param uid
     * @return
     */
    User getUserById(String uid);

    /**
     * 根据用户ID获取用户信息
     *
     * @param user
     * @return
     */
    User getUserById(User user);
}
