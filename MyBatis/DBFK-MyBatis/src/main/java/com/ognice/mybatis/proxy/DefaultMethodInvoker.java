package com.ognice.mybatis.proxy;

import com.ognice.mybatis.session.SqlSession;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/20
 */
@Data
@Accessors(chain = true)
public class DefaultMethodInvoker implements MapperMethodInvoker{
    MapperMethod method;
    private DefaultMethodInvoker(){}
    public DefaultMethodInvoker(MapperMethod method){
        this.method=method;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {

        final Object result = this.method.execute(sqlSession, args);
        return result;
    }
}
