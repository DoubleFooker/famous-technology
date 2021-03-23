package com.ognice.mybatis.proxy;

import com.ognice.mybatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/20
 */
public interface MapperMethodInvoker {
    Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable;
}