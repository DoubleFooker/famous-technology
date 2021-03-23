package com.ognice.mybatis.annotations;

import java.lang.annotation.*;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/20
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Mapper {
}
