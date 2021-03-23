package com.ognice.mybatis.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/20
 */
public class ResourceLoader {
    static ClassLoader defaultClassLoader = ClassLoader.getSystemClassLoader();

    /**
     * 加载运行环境配置资源
     * @param configFileName
     * @return
     * @throws IOException
     */
    public static InputStream loadXml(String configFileName) throws IOException {
        InputStream configStream = defaultClassLoader.getResourceAsStream(configFileName);
        if (configStream == null) {
            defaultClassLoader = Thread.currentThread().getContextClassLoader();
            configStream = defaultClassLoader.getResourceAsStream(configFileName);
        }
        if (configStream == null) {
            throw new IOException(configFileName + "not found!");
        }
        return configStream;
    }
}
