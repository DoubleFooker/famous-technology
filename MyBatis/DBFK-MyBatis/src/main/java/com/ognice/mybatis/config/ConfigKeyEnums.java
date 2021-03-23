package com.ognice.mybatis.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/21
 */
@Getter
@AllArgsConstructor
public enum ConfigKeyEnums {
    SQL_LOG("sql.log"),
    JDBC_DRIVER("jdbc.driver"),
    JDBC_URL("jdbc.url"),
    JDBC_USERNAME("jdbc.username"),
    JDBC_PWD("jdbc.pwd"),
    MAPPER_PACKAGES("mapper.packages");
    private String key;
}
