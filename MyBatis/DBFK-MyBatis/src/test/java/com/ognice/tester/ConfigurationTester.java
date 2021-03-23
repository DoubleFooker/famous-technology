package com.ognice.tester;

import com.ognice.mybatis.config.Configuration;
import com.ognice.mybatis.session.SqlSessionFactory;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/22
 */

public class ConfigurationTester {
    @Test
    public void testParse() throws IOException {
        Configuration configuration = new Configuration("mybatis-config.properties");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactory(configuration);
    }
}
