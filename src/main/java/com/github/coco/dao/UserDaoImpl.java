package com.github.coco.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Yan
 */
@Service
public class UserDaoImpl implements BaseDAO {
    @Resource
    private JdbcTemplate jdbcTemplate;
}
