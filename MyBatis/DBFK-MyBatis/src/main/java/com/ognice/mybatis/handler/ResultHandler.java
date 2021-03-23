package com.ognice.mybatis.handler;

import com.ognice.mybatis.uitls.Strings;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/21
 */
@Slf4j
public class ResultHandler {
    private Type returnType;

    private ResultHandler() {
    }

    public ResultHandler(Type returnType) {
        this.returnType = returnType;
    }
    public Type getReturnType(){
        return returnType;
    }
    public List<Object> result(ResultSet resultSet) throws SQLException {
        Type objectType = returnType;
        if (returnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) returnType;
            for (Type arg : pt.getActualTypeArguments()) {
                objectType = arg;
            }
        }
        final Class<?> returnClass = (Class<?>)objectType;
        List<Object> resultList = new ArrayList<>();
        while (resultSet.next()) {
            try {
                Object result = returnClass.newInstance();

                final Field[] fields = returnClass.getDeclaredFields();
                for (Field field : fields) {
                    final Class<?> type = field.getType();
                    final String fieldName = Strings.upperFirst(field.getName());
                    final Method method = returnClass.getMethod("set" + fieldName, type);
                    if (type == Integer.class) {
                        method.invoke(result, resultSet.getInt(fieldName));
                    }
                    if (type == Long.class) {
                        method.invoke(result, resultSet.getLong(fieldName));
                    }
                    if (type == String.class) {
                        method.invoke(result, resultSet.getString(fieldName));
                    }
                    if (type == Boolean.class) {
                        method.invoke(result, resultSet.getBoolean(fieldName));
                    }
                }
                resultList.add(result);
            } catch (Exception e) {
                log.error("result error", e);
                throw new RuntimeException("result set err", e);
            }
        }

        return resultList;
    }

}
