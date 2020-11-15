package com.github.coco.dao;

import com.github.coco.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Yan
 */
@Mapper
public interface UserDao extends BaseDAO {
    /**
     * 添加用户
     *
     * @param user
     * @return
     */
    @Insert("INSERT INTO t_user (username, salt, password, nickname, create_time) " +
            "VALUES (#{username}, #{salt}, #{password}, #{nickname}, #{createTime});")
    int insertUser(User user);

    /**
     * 删除用户
     *
     * @param user
     * @return
     */
    @Delete("DELETE FROM t_user WHERE uid = #{uid}")
    int deleteUser(User user);

    /**
     * 修改用户
     *
     * @param user
     * @return
     */
    @Update("UPDATE t_user SET ")
    int updateUser(User user);

    /**
     * 查询用户
     *
     * @param user
     * @return
     */
    @Select("SELECT * FROM t_user")
    List<User> selectUsers(User user);

    /**
     * 查询单用户
     *
     * @param user
     * @return
     */
    @Select("SELECT * FROM t_user WHERE username = #{username}")
    User selectUser(User user);
}
