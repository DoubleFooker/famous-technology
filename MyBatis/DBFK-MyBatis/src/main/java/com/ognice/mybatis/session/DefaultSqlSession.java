package com.ognice.mybatis.session;

import com.ognice.mybatis.config.ConfigKeyEnums;
import com.ognice.mybatis.config.Configuration;
import com.ognice.mybatis.proxy.MapperMethod;
import com.ognice.mybatis.proxy.MapperProxyFactory;
import com.ognice.mybatis.uitls.Strings;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/20
 */
@Slf4j
public class DefaultSqlSession implements SqlSession {
    private Configuration configuration;

    private DefaultSqlSession() {
    }

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <T> T getMapper(Class<T> mapperInterface) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) configuration.mapperMap.get(mapperInterface);
        return mapperProxyFactory.newInstance(this);
    }

    @Override
    public <T> T selectOne(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        final List<Object> objects = selectList(mapperMethod, param);
        if (objects.size() > 1) {
            throw new SQLException("result more than one");
        }
        return objects.size() == 0 ? null : (T) objects.get(0);
    }

    private void prepareStatement(PreparedStatement ps, List<Object> args) throws SQLException {
        for (int i = 0, size = args.size(); i < size; i++) {
            Object arg = args.get(i);
            if (arg instanceof Integer) {
                ps.setInt(i + 1, (Integer) arg);
            } else if (arg instanceof Long) {
                ps.setLong(i + 1, (Long) arg);

            } else if (arg instanceof Boolean) {
                ps.setBoolean(i + 1, (Boolean) arg);
            } else {
                ps.setString(i + 1, (String) arg);
            }
            // TODO 支持类型拓展
        }
    }


    @Override
    public <E> List<E> selectList(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        try (final PreparedStatement preparedStatement = getPrepareStatement(mapperMethod, param)) {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return (List<E>) mapperMethod.getResultHandler().result(resultSet);
            }
        }
    }

    @Override
    public int update(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        try (final PreparedStatement preparedStatement = getPrepareStatement(mapperMethod, param)) {
            return preparedStatement.executeUpdate();
        }
    }

    private PreparedStatement getPrepareStatement(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        final String sql = mapperMethod.getSql();
        final Connection connection = configuration.getDataSource().getConnection();
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        prepareStatement(preparedStatement, param);
        if ("true".equals(configuration.configMap.getOrDefault(ConfigKeyEnums.SQL_LOG.getKey(), "false"))) {
            log.info("execute sql:{}", preparedStatement.toString());
        }
        return preparedStatement;

    }

    @Override
    public int delete(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        return update(mapperMethod, param);
    }

    @Override
    public int insert(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        return update(mapperMethod, param);
    }


}
