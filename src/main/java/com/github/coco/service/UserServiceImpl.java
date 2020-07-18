package com.github.coco.service;

import com.github.coco.dao.UserDao;

import javax.annotation.Resource;

/**
 * @author Yan
 */
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;
}
