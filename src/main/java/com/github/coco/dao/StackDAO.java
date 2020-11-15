package com.github.coco.dao;

import com.github.coco.entity.Stack;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Yan
 */
@Mapper
public interface StackDAO extends BaseDAO {
    @Insert("INSERT INTO t_stack(id, name, type, endpoint) VALUES (#{id}, #{name}, #{type}, #{endpoint});")
    void insertStack(Stack stack);

    @Delete("DELETE FROM t_stack WHERE name = #{name} AND type = #{type} AND endpoint = #{endpoint};")
    int deleteStack(Stack stack);

    @Update("UPDATE t_stack SET name = #{name} WHERE name = #{name} AND type = #{type} AND endpoint = #{endpoint};")
    int updateStack(Stack stack);

    @Select("SELECT * FROM t_stack WHERE name = #{name} AND type = #{type} AND endpoint = #{endpoint};")
    Stack selectStack(Stack stack);

    @Select("SELECT * FROM t_stack WHERE endpoint = #{endpoint};")
    List<Stack> selectStacks(String endpoint);
}
