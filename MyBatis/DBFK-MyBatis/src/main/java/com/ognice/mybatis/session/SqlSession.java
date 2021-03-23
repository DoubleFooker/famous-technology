package com.ognice.mybatis.session;

import com.ognice.mybatis.proxy.MapperMethod;

import java.sql.SQLException;
import java.util.List;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/20
 */
public interface SqlSession {

    <T> T getMapper(Class<T> mapperInterface);

    <T> T selectOne(MapperMethod mapperMethod, List<Object> param) throws SQLException;

    <E> List<E> selectList(MapperMethod mapperMethod, List<Object> param) throws SQLException;

    int update(MapperMethod mapperMethod, List<Object> param) throws SQLException;

    int delete(MapperMethod mapperMethod, List<Object> param) throws SQLException;

    int insert(MapperMethod mapperMethod, List<Object> param) throws SQLException;
}
