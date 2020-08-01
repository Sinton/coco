package com.github.coco.dao;

import com.github.coco.entity.Host;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Yan
 */
@Mapper
public interface HostDAO extends BaseDAO {
    /**
     * 添加Host
     *
     * @param host
     */
    @Insert("INSERT INTO t_host(host, port, user, password) " +
            "VALUES (#{host}, #{port}, #{user}, #{password})")
    void insertHost(Host host);

    /**
     * 删除Host
     *
     * @param host
     * @return
     */
    @Delete("DELETE FROM t_host where id = #{id} AND host = #{host}")
    int deleteHost(Host host);

    /**
     * 修改Host
     *
     * @param host
     * @return
     */
    @Update("UPDATE t_host SET host = #{host} WHERE id = #{id}")
    int updateHost(Host host);

    /**
     * 查询主机
     *
     * @param host
     * @return
     */
    @Select("select * from t_host where id = #{id}")
    Host selectHost(Host host);

    /**
     * 查询Host
     *
     * @return
     */
    @Select("SELECT * FROM t_host")
    List<Host> selectHosts();
}
