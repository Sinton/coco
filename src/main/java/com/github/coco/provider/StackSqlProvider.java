package com.github.coco.provider;

import com.github.coco.entity.Stack;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * @author Yan
 */
public class StackSqlProvider {
    private static final String TABLE_NAME = "t_stack";

    public String insertStack(Stack stack) {
        SQL sql = new SQL().INSERT_INTO(TABLE_NAME);
        if (StringUtils.isNotBlank(stack.getName())) {
            sql.INTO_COLUMNS("name");
            sql.INTO_VALUES("#{name}");
        }
        if (stack.getType() != null) {
            sql.INTO_COLUMNS("type");
            sql.INTO_VALUES("#{type}");
        }
        if (StringUtils.isNotBlank(stack.getEndpoint())) {
            sql.INTO_COLUMNS("endpoint");
            sql.INTO_VALUES("#{endpoint}");
        }
        if (StringUtils.isNotBlank(stack.getSwarmId())) {
            sql.INTO_COLUMNS("swarm_id");
            sql.INTO_VALUES("#{swarm_id}");
        }
        return sql.toString();
    }

    public String deleteStack(Stack stack) {
        SQL sql = new SQL().DELETE_FROM(TABLE_NAME)
                           .WHERE("1 = 1")
                           .WHERE("id = #{id}");
        if (StringUtils.isNotBlank(stack.getName())) {
            sql.WHERE("name = #{name}");
        }
        if (StringUtils.isNotBlank(stack.getEndpoint())) {
            sql.WHERE("endpoint = #{endpoint}");
        }
        if (StringUtils.isNotBlank(stack.getSwarmId())) {
            sql.WHERE("swarm_id = #{swarmId}");
        }
        if (stack.getType() != null) {
            sql.WHERE("type = #{type}");
        }
        return sql.toString();
    }

    public String updateStack(Stack stack) {
        SQL sql = new SQL().UPDATE(TABLE_NAME)
                           .WHERE("1 = 1")
                           .WHERE("id = #{id}");
        if (StringUtils.isNotBlank(stack.getName())) {
            sql.SET("name = #{name}");
        }
        if (stack.getStatus() != null) {
            sql.SET("status = #{status}");
        }
        if (stack.getType() != null) {
            sql.SET("type = #{type}");
        }
        if (StringUtils.isNotBlank(stack.getEndpoint())) {
            sql.SET("endpoint = #{endpoint}");
        }
        return sql.toString();
    }

    public String selectStack(Stack stack) {
        SQL sql = new SQL().SELECT("*")
                           .FROM(TABLE_NAME)
                           .WHERE("1 = 1");
        if (stack.getId() != null) {
            sql.WHERE("id = #{id}");
        }
        if (StringUtils.isNotBlank(stack.getName())) {
            sql.WHERE("name = #{name}");
        }
        if (StringUtils.isNotBlank(stack.getEndpoint())) {
            sql.WHERE("endpoint = #{endpoint}");
        }
        if (StringUtils.isNotBlank(stack.getSwarmId())) {
            sql.WHERE("swarm_id = #{swarmId}");
        }
        if (stack.getType() != null) {
            sql.WHERE("type = #{type}");
        }
        return sql.toString();
    }
}
