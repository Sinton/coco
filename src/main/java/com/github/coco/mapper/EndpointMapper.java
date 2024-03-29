package com.github.coco.mapper;

import com.github.coco.entity.Endpoint;
import com.github.coco.provider.EndpointSqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Yan
 */
@Mapper
public interface EndpointMapper extends BaseMapper {
    /**
     * 添加Endpoint
     *
     * @param endpoint
     * @return
     */
    @InsertProvider(type = EndpointSqlProvider.class, method = "insertEndpoint")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insertEndpoint(Endpoint endpoint);

    /**
     * 删除Endpoint
     *
     * @param id
     * @return
     */
    @Delete("DELETE FROM t_endpoint WHERE id = #{id}")
    int deleteEndpoint(@Param("id") Integer id);

    /**
     * 修改Endpoint
     *
     * @param endpoint
     * @return
     */
    @UpdateProvider(type = EndpointSqlProvider.class, method = "updateEndpoint")
    int updateEndpoint(Endpoint endpoint);

    /**
     * 查询Endpoint
     *
     * @param endpoint
     * @return
     */
    @SelectProvider(type = EndpointSqlProvider.class, method = "selectEndpoint")
    Endpoint selectEndpoint(Endpoint endpoint);

    /**
     * 查询Endpoint列表
     *
     * @param offset
     * @param limit
     * @return
     */
    @Select("SELECT * FROM t_endpoint LIMIT #{offset},#{limit}")
    List<Endpoint> selectEndpoints(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询Endpoint总数
     *
     * @return
     */
    @Select("SELECT COUNT(*) AS total FROM t_endpoint")
    int selectEndpointTotal();
}
