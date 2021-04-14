package com.github.coco.mapper;

import com.github.coco.entity.Stack;
import com.github.coco.provider.StackSqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Yan
 */
@Mapper
public interface StackMapper extends BaseMapper {
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
     * 查询指定应用栈
     *
     * @param stack
     * @return
     */
    @SelectProvider(type = StackSqlProvider.class, method = "selectStack")
    Stack selectStack(Stack stack);

    /**
     * 查询应用栈集合
     *
     * @param stack
     * @param offset
     * @param limit
     * @return
     */
    @SelectProvider(type = StackSqlProvider.class, method = "selectStacks")
    List<Stack> selectStacks(@Param("stack") Stack stack, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询Stack总数
     *
     * @param stack
     * @return
     */
    @SelectProvider(type = StackSqlProvider.class, method = "selectStackTotal")
    int selectStackTotal(Stack stack);
}
