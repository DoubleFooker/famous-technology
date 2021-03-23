package com.ognice.mybatis.proxy;

import com.ognice.mybatis.annotations.Delete;
import com.ognice.mybatis.annotations.Insert;
import com.ognice.mybatis.annotations.Select;
import com.ognice.mybatis.annotations.Update;
import com.ognice.mybatis.enums.SqlCommandType;
import com.ognice.mybatis.handler.ParameterHandler;
import com.ognice.mybatis.handler.ResultHandler;
import com.ognice.mybatis.session.SqlSession;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * mapper代理类实现
 *
 * @author dbfk
 * @date 2021/3/20
 */
public class MapperProxy<T> implements InvocationHandler {
    private static final Set<Class<? extends Annotation>> STATEMENT_ANNOTATION_TYPES = Stream
            .of(Select.class, Update.class, Insert.class, Delete.class)
            .collect(Collectors.toSet());
    private final SqlSession sqlSession;
    private final Map<Method, MapperMethodInvoker> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
        this.sqlSession = sqlSession;
        this.methodCache = methodCache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        // 注解 获取
        Annotation sqlAnnotation = STATEMENT_ANNOTATION_TYPES.stream()
                .flatMap(x -> Arrays.stream(method.getAnnotationsByType(x))).findFirst().orElse(null);
        final Optional<SqlCommandType> sqlCommandType = getSqlCommandType(sqlAnnotation);
        if (sqlCommandType.isPresent()) {
            return methodCache.computeIfAbsent(method, m -> {
                final MapperMethod mapperMethod = new MapperMethod(sqlCommandType.get(),
                        new ParameterHandler(method),
                        new ResultHandler(method.getGenericReturnType()));
                mapperMethod.setSql(method, sqlAnnotation);
                return new DefaultMethodInvoker(mapperMethod);
            }).invoke(proxy, method, args, sqlSession);
        }
        throw new Throwable("err mapper method");
    }

    private Optional<SqlCommandType> getSqlCommandType(Annotation annotation) {
        SqlCommandType commandType = null;
        if (annotation instanceof Select) {
            commandType = SqlCommandType.SELECT;
        } else if (annotation instanceof Update) {
            commandType = SqlCommandType.UPDATE;
        } else if (annotation instanceof Insert) {
            commandType = SqlCommandType.INSERT;
        } else if (annotation instanceof Delete) {
            commandType = SqlCommandType.DELETE;
        }
        return Optional.ofNullable(commandType);
    }

}
