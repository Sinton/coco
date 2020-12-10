package com.github.coco.provider;

import com.github.coco.entity.Endpoint;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * @author Yan
 */
public class EndpointSqlProvider {
    private static final String TABLE_NAME = "t_endpoint";

    public String selectEndpoint(Endpoint endpoint) {
        SQL sql = new SQL();
        sql.SELECT("*")
           .FROM(TABLE_NAME)
           .WHERE("1=1");
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
        return sql.toString();
    }

    public String updateEndpoint(Endpoint endpoint) {
        SQL sql = new SQL();
        sql.UPDATE(TABLE_NAME)
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
}
