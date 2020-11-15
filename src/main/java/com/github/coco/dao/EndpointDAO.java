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
    @Insert("INSERT INTO t_endpoint(ip, port, name, url) " +
            "VALUES (#{ip}, #{port}, #{name}, #{url})")
    void insertEndpoint(Endpoint endpoint);

    /**
     * 删除Endpoint
     *
     * @param id
     * @return
     */
    @Delete("DELETE FROM t_endpoint where id = #{id}")
    int deleteEndpoint(String id);

    /**
     * 修改Endpoint
     *
     * @param endpoint
     * @return
     */
    @Update("UPDATE t_endpoint SET name = #{name}, port = #{port}, public_ip = #{publicIp}, " +
            "endpoint_url = #{endpointUrl}, endpoint_type = #{endpointType}, status = #{status}, " +
            "resources = #{resources}, tls_enable = #{tlsEnable}, tls_config = #{tlsConfig}, " +
            "docker_config = #{dockerConfig}, update_date_time = #{updateDateTime} " +
            "WHERE id = #{id}")
    int updateEndpoint(Endpoint endpoint);

    @Select("SELECT * FROM t_endpoint WHERE id = #{id}")
    Endpoint selectEndpoint(Endpoint endpoint);

    @Select("SELECT * FROM t_endpoint WHERE id = #{id}")
    Endpoint selectEndpointById(String id);

    /**
     * 查询Endpoint
     *
     * @return
     */
    @Select("SELECT * FROM t_endpoint")
    List<Endpoint> selectEndpoints();
}
