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
        if (StringUtils.isNotBlank(stack.getProjectPath())) {
            sql.INTO_COLUMNS("project_path");
            sql.INTO_VALUES("#{projectPath}");
        }
        if (stack.getOwner() != null) {
            sql.INTO_COLUMNS("owner");
            sql.INTO_VALUES("#{owner}");
        }
        if (stack.getInternal() != null) {
            sql.INTO_COLUMNS("internal");
            sql.INTO_VALUES("#{internal}");
        }
        return sql.toString();
    }

    public String deleteStack(Stack stack) {
        SQL sql = new SQL().DELETE_FROM(TABLE_NAME)
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
        if (stack.getType() != null) {
            sql.WHERE("type = #{type}");
        }
        if (stack.getOwner() != null) {
            sql.WHERE("owner = #{owner}");
        }
        return sql.toString();
    }

    public String updateStack(Stack stack) {
        SQL sql = new SQL().UPDATE(TABLE_NAME)
                           .WHERE("1 = 1");
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
        if (stack.getId() != null) {
            sql.WHERE("id = #{id}");
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
        if (stack.getOwner() != null) {
            sql.WHERE("owner = #{owner}");
        }
        if (stack.getType() != null) {
            sql.WHERE("type = #{type}");
        }
        return sql.toString();
    }

    public String selectStacks(Stack stack) {
        SQL sql = new SQL().SELECT("*")
                           .FROM(TABLE_NAME)
                           .WHERE("1 = 1");
        if (stack.getId() != null) {
            sql.WHERE("id = #{stack.id}");
        }
        if (StringUtils.isNotBlank(stack.getName())) {
            sql.WHERE("name = #{stack.name}");
        }
        if (StringUtils.isNotBlank(stack.getEndpoint())) {
            sql.WHERE("endpoint = #{stack.endpoint}");
        }
        if (stack.getOwner() != null) {
            sql.WHERE("owner = #{stack.owner}");
        }
        if (stack.getType() != null) {
            sql.WHERE("type = #{stack.type}");
        }
        if (stack.getOwner() != null) {
            sql.WHERE("owner = #{stack.owner}");
        }
        sql.OFFSET("#{offset}").LIMIT("#{limit}");
        return sql.toString();
    }

    public String selectStackTotal(Stack stack) {
        SQL sql = new SQL().SELECT("COUNT(*) AS total")
                           .FROM(TABLE_NAME)
                           .WHERE("1 = 1");
        if (StringUtils.isNotBlank(stack.getName())) {
            sql.WHERE("name = #{name}");
        }
        if (StringUtils.isNotBlank(stack.getEndpoint())) {
            sql.WHERE("endpoint = #{endpoint}");
        }
        if (stack.getOwner() != null) {
            sql.WHERE("owner = #{owner}");
        }
        if (stack.getType() != null) {
            sql.WHERE("type = #{type}");
        }
        if (stack.getOwner() != null) {
            sql.WHERE("owner = #{owner}");
        }
        return sql.toString();
    }
}
