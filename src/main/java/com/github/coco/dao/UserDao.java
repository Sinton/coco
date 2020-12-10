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
     * @param uid
     * @return
     */
    @Delete("DELETE FROM t_user WHERE uid = #{uid};")
    int deleteUser(@Param("uid") Integer uid);

    /**
     * 修改用户个人资料信息
     *
     * @param user
     * @return
     */
    @Update("UPDATE t_user SET nickname = #{nickname}, last_login_ip = #{lastLoginIp}, last_login_data = #{lastLoginDate} " +
            "WHERE uid = #{uid};")
    int updateUserPorfile(User user);

    /**
     * 修改用户密码
     *
     * @param user
     * @return
     */
    @Update("UPDATE t_user SET password = #{password} WHERE uid = #{uid};")
    int updateUserPassword(User user);

    /**
     * 查询用户
     *
     * @return
     */
    @Select("SELECT * FROM t_user")
    List<User> selectUsers();

    /**
     * 查询单用户
     *
     * @param user
     * @return
     */
    @Select("SELECT * FROM t_user WHERE username = #{username}")
    User selectUser(User user);
}
