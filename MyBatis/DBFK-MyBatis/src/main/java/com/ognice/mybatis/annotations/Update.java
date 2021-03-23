package com.ognice.mybatis.annotations;

import java.lang.annotation.*;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/21
 */

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Update {
    String value();
}
