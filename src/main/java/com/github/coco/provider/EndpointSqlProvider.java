package com.github.coco.provider;

import com.github.coco.entity.Endpoint;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * @author Yan
 */
public class EndpointSqlProvider {
    private static final String TABLE_NAME = "t_endpoint";

    public String insertEndpoint(Endpoint endpoint) {
        SQL sql = new SQL().INSERT_INTO(TABLE_NAME);
        if (StringUtils.isNotBlank(endpoint.getName())) {
            sql.INTO_COLUMNS("name");
            sql.INTO_VALUES("#{name}");
        }
        if (endpoint.getPublicIp() != null) {
            sql.INTO_COLUMNS("public_ip");
            sql.INTO_VALUES("#{publicIp}");
        }
        if (endpoint.getPort() != null) {
            sql.INTO_COLUMNS("port");
            sql.INTO_VALUES("#{port}");
        }
        if (endpoint.getEndpointType() != null) {
            sql.INTO_COLUMNS("endpoint_type");
            sql.INTO_VALUES("#{endpointType}");
        }
        if (StringUtils.isNotBlank(endpoint.getEndpointUrl())) {
            sql.INTO_COLUMNS("endpoint_url");
            sql.INTO_VALUES("#{endpointUrl}");
        }
        if (StringUtils.isNotBlank(endpoint.getDockerConfig())) {
            sql.INTO_COLUMNS("docker_config");
            sql.INTO_VALUES("#{dockerConfig}");
        }
        if (endpoint.getTlsEnable() != null) {
            sql.INTO_COLUMNS("tls_enable");
            sql.INTO_VALUES("#{tlsEnable}");
        }
        if (endpoint.getUpdateDateTime() != null) {
            sql.INTO_COLUMNS("update_date_time");
            sql.INTO_VALUES("#{updateDateTime}");
        }
        if (endpoint.getOwner() != null) {
            sql.INTO_COLUMNS("owner");
            sql.INTO_VALUES("#{owner}");
        }
        return sql.toString();
    }

    public String updateEndpoint(Endpoint endpoint) {
        SQL sql = new SQL().UPDATE(TABLE_NAME)
                           .WHERE("1 = 1")
                           .WHERE("id = #{id}");
        if (StringUtils.isNotBlank(endpoint.getName())) {
            sql.SET("name = #{name}");
        }
        if (endpoint.getPort() != null) {
            sql.SET("port = #{port}");
        }
        if (StringUtils.isNotBlank(endpoint.getPublicIp())) {
            sql.SET("public_ip = #{publicIp}");
        }
        if (StringUtils.isNotBlank(endpoint.getEndpointUrl())) {
            sql.SET("endpoint_url = #{endpointUrl}");
        }
        if (endpoint.getEndpointType() != null) {
            sql.SET("endpoint_type = #{endpointType}");
        }
        if (endpoint.getStatus() != null) {
            sql.SET("status = #{status}");
        }
        if (StringUtils.isNotBlank(endpoint.getResources())) {
            sql.SET("resources = #{resources}");
        }
        if (endpoint.getTlsEnable() != null) {
            sql.SET("tls_enable = #{tlsEnable}");
        }
        if (StringUtils.isNotBlank(endpoint.getTlsConfig())) {
            sql.SET("tls_config = #{tlsConfig}");
        }
        if (StringUtils.isNotBlank(endpoint.getDockerConfig())) {
            sql.SET("docker_config = #{dockerConfig}");
        }
        if (endpoint.getUpdateDateTime() != null) {
            sql.SET("update_date_time = #{updateDateTime}");
        }
        return sql.toString();
    }

    public String selectEndpoint(Endpoint endpoint) {
        SQL sql = new SQL().SELECT("*")
                           .FROM(TABLE_NAME)
                           .WHERE("1 = 1");
        if (endpoint.getId() != null) {
            sql.WHERE("id = #{id}");
        }
        if (StringUtils.isNotBlank(endpoint.getName())) {
            sql.WHERE("name = #{name}");
        }
        if (StringUtils.isNotBlank(endpoint.getPublicIp())) {
            sql.WHERE("public_ip = #{publicIp}");
        }
        if (StringUtils.isNotBlank(endpoint.getEndpointUrl())) {
            sql.WHERE("endpoint_url = #{endpointUrl}");
        }
        if (endpoint.getEndpointType() != null) {
            sql.WHERE("endpoint_type = #{endpointType}");
        }
        if (endpoint.getStatus() != null) {
            sql.WHERE("status = #{status}");
        }
        if (endpoint.getOwner() != null) {
            sql.WHERE("owner = #{owner}");
        }
        return sql.toString();
    }
}
