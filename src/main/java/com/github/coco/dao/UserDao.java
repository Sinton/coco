package com.github.coco.dao;

import com.github.coco.entity.User;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Yan
 */
@Mapper
public interface UserDao extends BaseDAO {
    @Data
    class QueryUser {
        private String uid;
        private String nickname;
        private String userName;
        private String email;
    }

    /**
     * 查询用户
     *
     * @param queryUser
     * @return
     */
    @Select("select * from t_user")
    List<User> selectUser(QueryUser queryUser);
}
