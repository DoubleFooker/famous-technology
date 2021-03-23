### 手写简版MyBatis
关注公众号《每天学点CRUD》，更多基础知识、面试题答疑学习分享。
### 基本执行步
整理了Mybatis执行原理，通过手写简版Mybatis加深理解。
Mybatis方法执行流程
1.配置解析
2.获取会话
3.获取Mapper代理对象
4.执行方法
### 配置解析
`Configuration`类
```java
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
```
通过配置Mapper.package制定扫描包，接口类上添加注解标识为使用的Mapper。新建代理类封装mapper
#### MapperProxyFactory
```java
public class MapperProxyFactory<T> {
    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public T newInstance(SqlSession session) {
        MapperProxy<T> mapperProxy = new MapperProxy<T>(session, mapperInterface, methodCache);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }
}
```
#### MapperProxy
mapper动态代理类
```java
public class MapperProxy<T> implements InvocationHandler {
    private static final Set<Class<? extends Annotation>> STATEMENT_ANNOTATION_TYPES = Stream
            .of(Select.class, Update.class, Insert.class, Delete.class)
            .collect(Collectors.toSet());
    private final SqlSession sqlSession;
    private final Map<Method, MapperMethodInvoker> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
        this.sqlSession = sqlSession;
        this.methodCache = methodCache;
    }

    /**
     * 方法执行
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        // 注解 获取
        Annotation sqlAnnotation = STATEMENT_ANNOTATION_TYPES.stream()
                .flatMap(x -> Arrays.stream(method.getAnnotationsByType(x))).findFirst().orElse(null);
        final Optional<SqlCommandType> sqlCommandType = getSqlCommandType(sqlAnnotation);
        if (sqlCommandType.isPresent()) {
            return methodCache.computeIfAbsent(method, m -> {
                final MapperMethod mapperMethod = new MapperMethod(sqlCommandType.get(),
                        new ParameterHandler(method),
                        new ResultHandler(method.getGenericReturnType()));
                mapperMethod.setSql(method, sqlAnnotation);
                return new DefaultMethodInvoker(mapperMethod);
            }).invoke(proxy, method, args, sqlSession);
        }
        throw new Throwable("err mapper method");
    }

    private Optional<SqlCommandType> getSqlCommandType(Annotation annotation) {
        SqlCommandType commandType = null;
        if (annotation instanceof Select) {
            commandType = SqlCommandType.SELECT;
        } else if (annotation instanceof Update) {
            commandType = SqlCommandType.UPDATE;
        } else if (annotation instanceof Insert) {
            commandType = SqlCommandType.INSERT;
        } else if (annotation instanceof Delete) {
            commandType = SqlCommandType.DELETE;
        }
        return Optional.ofNullable(commandType);
    }

}
```
#### mapper方法入参映射解析
`ParameterHandler`处理mapper方法参数与sql中参数占位符的对应关系
```java
public class ParameterHandler {
    private SortedMap<String, Integer> paramNameMap = new TreeMap<>();

    public SortedMap<String, Integer> getParamMap() {
        return paramNameMap;
    }

    private ParameterHandler() {
    }

    public ParameterHandler(Method method) {
        initParam(method);

    }

    private void initParam(Method method) {
        final Class<?>[] paramTypes = method.getParameterTypes();
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        final SortedMap<String, Integer> map = new TreeMap<>();
        int paramCount = paramTypes.length;
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            String name = null;
            for (Annotation annotation : paramAnnotations[paramIndex]) {
                if (annotation instanceof Param) {
                    name = ((Param) annotation).value();
                    break;
                }
            }
            if (name == null) {
                name = "" + map.size();
            }
            map.put(name, paramIndex);
        }
        paramNameMap = Collections.unmodifiableSortedMap(map);
    }

    public void setParam(PreparedStatement ps, Object[] args) throws SQLException {
        for (int i = 0, size = args.length; i < size; i++) {
            Object arg = args[i];
            if (arg instanceof Integer) {
                ps.setInt(i + 1, (Integer) arg);
            } else if (arg instanceof Long) {
                ps.setLong(i + 1, (Long) arg);

            } else if (arg instanceof Boolean) {
                ps.setBoolean(i + 1, (Boolean) arg);
            } else {
                ps.setString(i + 1, (String) arg);
            }
            // TODO 支持类型拓展
        }
    }

}
```
#### mapper方法返回对象映射解析
`ResultHandler`处理执行结果出参的设置
```java
@Slf4j
public class ResultHandler {
    private Type returnType;

    private ResultHandler() {
    }

    public ResultHandler(Type returnType) {
        this.returnType = returnType;
    }
    public Type getReturnType(){
        return returnType;
    }
    public List<Object> result(ResultSet resultSet) throws SQLException {
        Type objectType = returnType;
        if (returnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) returnType;
            for (Type arg : pt.getActualTypeArguments()) {
                objectType = arg;
            }
        }
        final Class<?> returnClass = (Class<?>)objectType;
        List<Object> resultList = new ArrayList<>();
        while (resultSet.next()) {
            try {
                Object result = returnClass.newInstance();

                final Field[] fields = returnClass.getDeclaredFields();
                for (Field field : fields) {
                    final Class<?> type = field.getType();
                    final String fieldName = Strings.upperFirst(field.getName());
                    final Method method = returnClass.getMethod("set" + fieldName, type);
                    if (type == Integer.class) {
                        method.invoke(result, resultSet.getInt(fieldName));
                    }
                    if (type == Long.class) {
                        method.invoke(result, resultSet.getLong(fieldName));
                    }
                    if (type == String.class) {
                        method.invoke(result, resultSet.getString(fieldName));
                    }
                    if (type == Boolean.class) {
                        method.invoke(result, resultSet.getBoolean(fieldName));
                    }
                }
                resultList.add(result);
            } catch (Exception e) {
                log.error("result error", e);
                throw new RuntimeException("result set err", e);
            }
        }

        return resultList;
    }

}

```
### openSession 获取会话
创建默认session对象
```java
@Slf4j
public class DefaultSqlSession implements SqlSession {
    private Configuration configuration;

    private DefaultSqlSession() {
    }

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <T> T getMapper(Class<T> mapperInterface) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) configuration.mapperMap.get(mapperInterface);
        return mapperProxyFactory.newInstance(this);
    }

    @Override
    public <T> T selectOne(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        final List<Object> objects = selectList(mapperMethod, param);
        if (objects.size() > 1) {
            throw new SQLException("result more than one");
        }
        return objects.size() == 0 ? null : (T) objects.get(0);
    }

    private void prepareStatement(PreparedStatement ps, List<Object> args) throws SQLException {
        for (int i = 0, size = args.size(); i < size; i++) {
            Object arg = args.get(i);
            if (arg instanceof Integer) {
                ps.setInt(i + 1, (Integer) arg);
            } else if (arg instanceof Long) {
                ps.setLong(i + 1, (Long) arg);

            } else if (arg instanceof Boolean) {
                ps.setBoolean(i + 1, (Boolean) arg);
            } else {
                ps.setString(i + 1, (String) arg);
            }
            // TODO 支持类型拓展
        }
    }


    @Override
    public <E> List<E> selectList(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        try (final PreparedStatement preparedStatement = getPrepareStatement(mapperMethod, param)) {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return (List<E>) mapperMethod.getResultHandler().result(resultSet);
            }
        }
    }

    @Override
    public int update(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        try (final PreparedStatement preparedStatement = getPrepareStatement(mapperMethod, param)) {
            return preparedStatement.executeUpdate();
        }
    }

    private PreparedStatement getPrepareStatement(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        final String sql = mapperMethod.getSql();
        final Connection connection = configuration.getDataSource().getConnection();
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        prepareStatement(preparedStatement, param);
        if ("true".equals(configuration.configMap.getOrDefault(ConfigKeyEnums.SQL_LOG.getKey(), "false"))) {
            log.info("execute sql:{}", preparedStatement.toString());
        }
        return preparedStatement;

    }

    @Override
    public int delete(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        return update(mapperMethod, param);
    }

    @Override
    public int insert(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        return update(mapperMethod, param);
    }


}

```
### getMapper 获取Mapper
获取`Configuration`扫描的`Mapper`代理对象
```java
  @Override
    public <T> T getMapper(Class<T> mapperInterface) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) configuration.mapperMap.get(mapperInterface);
        return mapperProxyFactory.newInstance(this);
    }
```
### 执行mapper方法
Mapper执行流程是代理对象`MapperProxy`的调用过程
`MapperProxy#invoke`->`MapperMethod#execute`->`SqlSession#对应方法`
#### JDBC执行
`DefaultSqlSession`,处理jdbc执行
```java
@Slf4j
public class DefaultSqlSession implements SqlSession {
    private Configuration configuration;

    private DefaultSqlSession() {
    }

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <T> T getMapper(Class<T> mapperInterface) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) configuration.mapperMap.get(mapperInterface);
        return mapperProxyFactory.newInstance(this);
    }

    @Override
    public <T> T selectOne(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        final List<Object> objects = selectList(mapperMethod, param);
        if (objects.size() > 1) {
            throw new SQLException("result more than one");
        }
        return objects.size() == 0 ? null : (T) objects.get(0);
    }

    private void prepareStatement(PreparedStatement ps, List<Object> args) throws SQLException {
        for (int i = 0, size = args.size(); i < size; i++) {
            Object arg = args.get(i);
            if (arg instanceof Integer) {
                ps.setInt(i + 1, (Integer) arg);
            } else if (arg instanceof Long) {
                ps.setLong(i + 1, (Long) arg);

            } else if (arg instanceof Boolean) {
                ps.setBoolean(i + 1, (Boolean) arg);
            } else {
                ps.setString(i + 1, (String) arg);
            }
            // TODO 支持类型拓展
        }
    }


    @Override
    public <E> List<E> selectList(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        try (final PreparedStatement preparedStatement = getPrepareStatement(mapperMethod, param)) {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return (List<E>) mapperMethod.getResultHandler().result(resultSet);
            }
        }
    }

    @Override
    public int update(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        try (final PreparedStatement preparedStatement = getPrepareStatement(mapperMethod, param)) {
            return preparedStatement.executeUpdate();
        }
    }

    private PreparedStatement getPrepareStatement(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        final String sql = mapperMethod.getSql();
        final Connection connection = configuration.getDataSource().getConnection();
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        prepareStatement(preparedStatement, param);
        if ("true".equals(configuration.configMap.getOrDefault(ConfigKeyEnums.SQL_LOG.getKey(), "false"))) {
            log.info("execute sql:{}", preparedStatement.toString());
        }
        return preparedStatement;

    }

    @Override
    public int delete(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        return update(mapperMethod, param);
    }

    @Override
    public int insert(MapperMethod mapperMethod, List<Object> param) throws SQLException {
        return update(mapperMethod, param);
    }


}
```
附上git项目地址
[主流框架技术解构](https://github.com/DoubleFooker/famous-technology)
