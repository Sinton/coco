package com.github.coco.service.impl;

import com.github.coco.dao.UserDao;
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
    private UserDao userDao;

    @Override
    public void createUser(User user) {
        userDao.insertUser(user);
    }

    @Override
    public User getUserById(Integer uid) {
        User user = new User();
        user.setUid(uid);
        return userDao.selectUser(user);
    }

    @Override
    public User getUserByName(String username) {
        User user = new User();
        user.setUsername(username);
        return userDao.selectUser(user);
    }

    @Override
    public User getUserByLogin(String username, String password) {
        return null;
    }
}
