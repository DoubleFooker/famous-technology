package com.ognice.mapper;

import com.ognice.entity.TestEntity;
import com.ognice.mybatis.annotations.*;

import java.util.List;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/23
 */
@Mapper
public interface TestMapper {
    @Select("SELECT * FROM test limit 1")
    TestEntity select();

    @Select("SELECT * FROM test where name=#{0}")
    List<TestEntity> selectList(String name);

    @Insert("Insert into test (name) value (#{item.name})")
    int insert(@Param("item") TestEntity entity);

    @Update("Update test set name=#{name} where name=#{oldName}")
    int update(@Param("name") String name, @Param("oldName") String oldName);

    @Delete("delete FROM test where name=#{0}")
    int delete(String name);
}
