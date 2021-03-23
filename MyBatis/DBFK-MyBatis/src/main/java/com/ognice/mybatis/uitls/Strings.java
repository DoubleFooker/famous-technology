package com.ognice.mybatis.uitls;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/22
 */
@Data
@Accessors(chain = true)
public class Strings {
    public static String upperFirst(String src) {
        char[] cs = src.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    /**
     * 下划线转驼峰
     *
     * @param src
     * @return
     */
    public static String lineToHump(String src) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] cs = src.toCharArray();
        boolean upper = false;
        for (char c : cs) {
            if (c == '_') {
                upper = true;
            } else {
                if (upper) {
                    stringBuilder.append(c -= 32);
                } else {
                    stringBuilder.append(c);

                }
                upper = false;
            }

        }
        return stringBuilder.toString();
    }
}
