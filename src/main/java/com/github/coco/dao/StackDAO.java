package com.github.coco.dao;

import com.github.coco.entity.Stack;
import com.github.coco.provider.StackSqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Yan
 */
@Mapper
public interface StackDAO extends BaseDAO {
    /**
     * 添加应用栈
     *
     * @param stack
     * @return
     */
    @InsertProvider(type = StackSqlProvider.class, method = "insertStack")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insertStack(Stack stack);

    /**
     * 删除应用栈
     *
     * @param stack
     * @return
     */
    @DeleteProvider(type = StackSqlProvider.class, method = "deleteStack")
    int deleteStack(Stack stack);

    /**
     * 更新应用栈
     *
     * @param stack
     * @return
     */
    @UpdateProvider(type = StackSqlProvider.class, method = "updateStack")
    int updateStack(Stack stack);

    /**
     * 查询应用栈
     *
     * @param stack
     * @return
     */
    @SelectProvider(type = StackSqlProvider.class, method = "selectStack")
    Stack selectStack(Stack stack);

    @Select("SELECT * FROM t_stack WHERE endpoint = #{endpoint};")
    List<Stack> selectStacks(@Param("endpoint") String endpoint);

    /*@Select("SELECT * FROM t_endpoint LIMIT #{offset},#{limit}")
    List<Stack> selectStacks(@Param("offset") int offset, @Param("limit") int limit);*/

    /**
     * 查询Stack总数
     *
     * @return
     */
    @Select("SELECT COUNT(*) AS total FROM t_stack")
    int selectStackTotal();
}
