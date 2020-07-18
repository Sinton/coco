package com.github.coco.dao;

import com.github.coco.entity.Endpoint;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Yan
 */
@Mapper
public interface EndpointDAO extends BaseDAO {
    /**
     * 添加Endpoint
     *
     * @param endpoint
     */
    @Insert("INSERT INTO t_endpoint(host, port, user, password) " +
            "VALUES (#{host}, #{port}, #{user}, #{password})")
    void insertEndpoint(Endpoint endpoint);

    /**
     * 删除Endpoint
     *
     * @param endpoint
     * @return
     */
    @Delete("DELETE FROM t_endpoint where id = #{id} AND host = #{host}")
    int deleteEndpoint(Endpoint endpoint);

    /**
     * 修改Endpoint
     *
     * @param endpoint
     * @return
     */
    @Update("UPDATE t_endpoint SET host = #{host} WHERE id = #{id}")
    int updateEndpoint(Endpoint endpoint);

    /**
     * 查询Endpoint
     *
     * @return
     */
    @Select("SELECT * FROM t_endpoint")
    List<Endpoint> selectEndpoint();
}
