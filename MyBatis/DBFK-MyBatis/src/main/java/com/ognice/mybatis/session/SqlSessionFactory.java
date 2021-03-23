package com.ognice.mybatis.session;

import com.ognice.mybatis.proxy.MapperProxyFactory;
import com.ognice.mybatis.config.Configuration;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/20
 */
@Data
@Accessors(chain = true)
public class SqlSessionFactory {
    Configuration configuration;

    private SqlSessionFactory() {
    }

    public SqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }
    public SqlSession openSession(){
        return new DefaultSqlSession(configuration);
    }

}
