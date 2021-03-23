package com.ognice.mybatis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/20
 */
@Getter
@AllArgsConstructor
public enum SqlCommandType {
    INSERT,
    SELECT,
    SELECT_List,
    UPDATE,
    DELETE,
    UNKNOWN
}
