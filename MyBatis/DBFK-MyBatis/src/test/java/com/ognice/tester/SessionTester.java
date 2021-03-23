package com.ognice.tester;

import com.ognice.entity.TestEntity;
import com.ognice.mapper.TestMapper;
import com.ognice.mybatis.config.Configuration;
import com.ognice.mybatis.session.SqlSession;
import com.ognice.mybatis.session.SqlSessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/23
 */
public class SessionTester {
    //1
    Configuration configuration;
    SqlSessionFactory sqlSessionFactory;
    private static String TESTNAME="testName";

    @Before
    public void setUp() throws IOException {
        configuration = new Configuration("mybatis-config.properties");
        sqlSessionFactory = new SqlSessionFactory(configuration);
    }


    @Test
    public void insert() {
        final SqlSession sqlSession = sqlSessionFactory.openSession();
        final TestMapper testMapper = sqlSession.getMapper(TestMapper.class);
        TestEntity testEntity = new TestEntity();
        testEntity.setName(TESTNAME);
        Assert.assertTrue(testMapper.insert(testEntity) > 0);

    }

    @Test
    public void update() {
        final SqlSession sqlSession = sqlSessionFactory.openSession();
        final TestMapper testMapper = sqlSession.getMapper(TestMapper.class);
        Assert.assertTrue(testMapper.update("newName", TESTNAME) > 0);
    }

    @Test
    public void selectOne() {
        final SqlSession sqlSession = sqlSessionFactory.openSession();
        final TestMapper testMapper = sqlSession.getMapper(TestMapper.class);
        final TestEntity one = testMapper.select();
        Assert.assertNotNull(one);
    }

    @Test
    public void selectList() {
        final SqlSession sqlSession = sqlSessionFactory.openSession();
        final TestMapper testMapper = sqlSession.getMapper(TestMapper.class);
        final List<TestEntity> testEntities = testMapper.selectList(TESTNAME);
        Assert.assertNotNull(testEntities);
    }

    @Test
    public void delete() {
        final SqlSession sqlSession = sqlSessionFactory.openSession();
        final TestMapper testMapper = sqlSession.getMapper(TestMapper.class);
        Assert.assertTrue(testMapper.delete(TESTNAME) > 0);

    }
}
