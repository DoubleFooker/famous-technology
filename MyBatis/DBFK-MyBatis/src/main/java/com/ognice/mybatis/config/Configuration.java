package com.ognice.mybatis.config;

import com.ognice.mybatis.annotations.Mapper;
import com.ognice.mybatis.proxy.MapperProxyFactory;
import com.ognice.mybatis.session.DefaultDataSource;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * some desc
 *
 * @author dbfk
 * @date 2021/3/20
 */
public class Configuration {
    /**
     * 数据源配置
     */
    DataSource dataSource;
    /**
     * 配置信息
     */
    public final Map<String, Object> configMap = new HashMap<>();
    /**
     * Mapper映射
     */
    public final Map<Class<?>, MapperProxyFactory<?>> mapperMap = new ConcurrentHashMap<>();

    private Configuration() {
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Configuration(String configFileName) throws IOException {
        Properties properties = new Properties();
        // 配置文件load
        try (InputStream in = Configuration.class.getClassLoader().getResourceAsStream(configFileName)) {
            properties.load(in);
        }
        // 配置型配置
        for (ConfigKeyEnums configKey : ConfigKeyEnums.values()) {
            configMap.put(configKey.getKey(), properties.get(configKey.getKey()));
        }
        // mapper登记
        final String packages = configMap.get(ConfigKeyEnums.MAPPER_PACKAGES.getKey()).toString();
        if (packages == null || packages.length() == 0) {
            throw new IOException("packages can't not be bank");
        }
        final String[] packageArr = packages.split(",");
        for (String pkg : packageArr) {
            if (pkg != null && pkg.length() > 0) {
                scanMapper(pkg);
            }
        }
        // 数据源创建
        this.dataSource = new DefaultDataSource(configMap.get(ConfigKeyEnums.JDBC_DRIVER.getKey()).toString(),
                configMap.get(ConfigKeyEnums.JDBC_URL.getKey()).toString(),
                configMap.get(ConfigKeyEnums.JDBC_USERNAME.getKey()).toString(),
                configMap.get(ConfigKeyEnums.JDBC_PWD.getKey()).toString());
    }
    /**
     * @param packageName
     */
    private void scanMapper(String packageName) {
        // 要扫描的包
        Reflections reflections = new Reflections(ClasspathHelper.forPackage(packageName, this.getClass().getClassLoader()),
                new TypeAnnotationsScanner(),
                new SubTypesScanner(false));
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(Mapper.class);
        // 存放url和ExecutorBean的对应关系
        for (Class<?> clazz : classesList) {
            MapperProxyFactory<?> mapperProxyFactory = new MapperProxyFactory<>(clazz);
            mapperMap.put(clazz, mapperProxyFactory);
        }
    }


}
