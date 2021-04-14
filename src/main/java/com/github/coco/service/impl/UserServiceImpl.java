package com.github.coco.service.impl;

import com.github.coco.mapper.UserMapper;
import com.github.coco.entity.User;
import com.github.coco.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Yan
 */
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public void createUser(User user) {
        userMapper.insertUser(user);
    }

    @Override
    public User getUserById(Integer uid) {
        User user = new User();
        user.setUid(uid);
        return userMapper.selectUser(user);
    }

    @Override
    public User getUserByName(String username) {
        User user = new User();
        user.setUsername(username);
        return userMapper.selectUser(user);
    }

    @Override
    public User getUserByLogin(String username, String password) {
        return null;
    }
}
