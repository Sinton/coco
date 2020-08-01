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
    public User getUserById(String uid) {
        User user = new User();
        user.setUid(uid);
        return userDao.selectUser(user);
    }

    @Override
    public User getUserById(User user) {
        return userDao.selectUser(user);
    }
}
