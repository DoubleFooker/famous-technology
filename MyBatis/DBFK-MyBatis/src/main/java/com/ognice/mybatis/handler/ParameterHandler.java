package com.ognice.mybatis.handler;

import com.ognice.mybatis.annotations.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/21
 */
public class ParameterHandler {
    private SortedMap<String, Integer> paramNameMap = new TreeMap<>();

    public SortedMap<String, Integer> getParamMap() {
        return paramNameMap;
    }

    private ParameterHandler() {
    }

    public ParameterHandler(Method method) {
        initParam(method);

    }

    private void initParam(Method method) {
        final Class<?>[] paramTypes = method.getParameterTypes();
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        final SortedMap<String, Integer> map = new TreeMap<>();
        int paramCount = paramTypes.length;
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            String name = null;
            for (Annotation annotation : paramAnnotations[paramIndex]) {
                if (annotation instanceof Param) {
                    name = ((Param) annotation).value();
                    break;
                }
            }
            if (name == null) {
                name = "" + map.size();
            }
            map.put(name, paramIndex);
        }
        paramNameMap = Collections.unmodifiableSortedMap(map);
    }

    public void setParam(PreparedStatement ps, Object[] args) throws SQLException {
        for (int i = 0, size = args.length; i < size; i++) {
            Object arg = args[i];
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

}
