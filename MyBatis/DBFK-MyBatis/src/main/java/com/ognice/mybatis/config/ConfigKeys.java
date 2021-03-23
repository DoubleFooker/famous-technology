package com.ognice.mybatis.config;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/20
 */
@Data
@Accessors(chain = true)
public class ConfigKeys {
    public static String JDBC_DRIVER="jdbc.driver";
    public static String JDBC_URL="jdbc.url";
    public static String JDBC_USERNAME="jdbc.username";
    public static String JDBC_PWD="jdbc.pwd";
    public static String MAPPER_PACKAGES="mapper.packages";
}
