package com.github.coco.service;

import com.github.coco.entity.User;

/**
 * @author Yan
 */
public interface UserService {
    /**
     * 创建用户
     *
     * @param user
     */
    void createUser(User user);

    /**
     * 根据用户ID获取用户信息
     *
     * @param uid
     * @return
     */
    User getUserById(Integer uid);

    /**
     * 根据用户名获取用户信息
     *
     * @param username
     * @return
     */
    User getUserByName(String username);

    User getUserByLogin(String username, String password);
}
