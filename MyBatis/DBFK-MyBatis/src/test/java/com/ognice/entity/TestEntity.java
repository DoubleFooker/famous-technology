package com.ognice.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/23
 */
@Data
@Accessors(chain = true)
public class TestEntity {
    private Long id;
    private String name;
}
