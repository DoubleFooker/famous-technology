/**
 * Copyright 2009-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ognice.mybatis.proxy;


import com.ognice.mybatis.annotations.Delete;
import com.ognice.mybatis.annotations.Insert;
import com.ognice.mybatis.annotations.Select;
import com.ognice.mybatis.annotations.Update;
import com.ognice.mybatis.enums.SqlCommandType;
import com.ognice.mybatis.handler.ParameterHandler;
import com.ognice.mybatis.handler.ResultHandler;
import com.ognice.mybatis.session.SqlSession;
import com.ognice.mybatis.uitls.Strings;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Lasse Voss
 * @author Kazuki Shimizu
 */
@Slf4j
public class MapperMethod {
    private SqlCommandType type;
    private ParameterHandler parameterHandler;
    private ResultHandler resultHandler;
    private String sql;
    private List<String> paramNames = new ArrayList<>();
    private boolean returnMany=false;

    private MapperMethod() {
    }

    public String getSql() {
        return this.sql;
    }

    public ResultHandler getResultHandler() {
        return this.resultHandler;
    }

    public MapperMethod(SqlCommandType type, ParameterHandler paramHandle, ResultHandler resultHandler) {
        this.type = type;
        this.parameterHandler = paramHandle;
        this.resultHandler = resultHandler;
        final Type returnType = resultHandler.getReturnType();
        if (returnType instanceof ParameterizedType) {
            returnMany=true;
        }

    }

    public Object execute(SqlSession sqlSession, Object[] args) throws SQLException {
        final List<Object> objects = args2ParamValue(args);
        switch (type) {
            case DELETE:
                return sqlSession.delete(this, objects);
            case INSERT:
                return sqlSession.insert(this, objects);
            case UPDATE:
                return sqlSession.update(this, objects);
            case SELECT:
                if(returnMany){
                 return    sqlSession.selectList(this,objects);
                }
                return sqlSession.selectOne(this, objects);
            default:
        }
        return null;

    }

    private List<Object> args2ParamValue(Object[] args) {
        final SortedMap<String, Integer> paramMap = parameterHandler.getParamMap();
        List<Object> values = new ArrayList<>();
        for (final String paramName : paramNames) {
            if (paramName.contains(".")) {
                final String[] params = paramName.split("\\.");
                final String param = params[0];
                final Integer paramIndex = paramMap.get(param);
                final Object object = args[paramIndex];
                try {
                    final Method method = object.getClass().getMethod("get" + Strings.upperFirst(params[1]));
                    final Object value = method.invoke(object);
                    values.add(value);
                } catch (Throwable t) {
                    log.error("get param error!param:{}",paramName,t);
                    throw new RuntimeException("no get method", t);
                }
            } else {
                final Integer paramIndex = paramMap.get(paramName);
                if (paramIndex == null || paramIndex > args.length) {
                    throw new RuntimeException("index size not match");
                }
                values.add(args[paramIndex]);
            }
        }

        return values;
    }

    /**
     * 获取方法执行sql
     *
     * @param method
     * @param annotation
     * @return
     */
    public void setSql(Method method, Annotation annotation) {
        String openToken = "#{";
        String closeToken = "}";
        String preSql = "";
        if (annotation instanceof Select) {
            final Select select = (Select) annotation;
            preSql = select.value();
        } else if (annotation instanceof Update) {
            final Update update = (Update) annotation;
            preSql = update.value();
        } else if (annotation instanceof Insert) {
            final Insert insert = (Insert) annotation;
            preSql = insert.value();
        } else if (annotation instanceof Delete) {
            final Delete delete = (Delete) annotation;
            preSql = delete.value();
        }
        if (preSql.isEmpty()) {
            this.sql = "";
            return;
        }
        // search open token
        int start = preSql.indexOf(openToken);
        if (start == -1) {
            this.sql = preSql;
            return;
        }
        char[] src = preSql.toCharArray();
        int offset = 0;
        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        do {
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // found open token. let's search close token.
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                int end = preSql.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        // this close token is escaped. remove the backslash and continue.
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = preSql.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        break;
                    }
                }
                if (end == -1) {
                    // close token was not found.
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    final String paramNameString = preSql.substring(start + openToken.length(), end - closeToken.length() + 1);
                    paramNames.add(paramNameString);
                    builder.append("?");
                    offset = end + closeToken.length();
                }
            }
            start = preSql.indexOf(openToken, offset);
        } while (start > -1);
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        this.sql = builder.toString();
    }


}
