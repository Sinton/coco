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
    @Insert("INSERT FROM t_user () VALUES ()")
    int insertUser(User user);

    /**
     * 删除用户
     *
     * @param user
     * @return
     */
    @Delete("Delete FROM t_user where ")
    int deleteUser(User user);

    /**
     * 修改用户
     *
     * @param user
     * @return
     */
    @Update("UPDATE t_user set ")
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
    @Select("SELECT * FROM t_user WHERE uid = #{uid}")
    User selectUser(User user);
}
