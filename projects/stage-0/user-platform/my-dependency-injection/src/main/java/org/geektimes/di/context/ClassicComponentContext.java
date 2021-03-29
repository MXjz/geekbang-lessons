package org.geektimes.di.context;

import org.geektimes.di.function.ThrowableAction;
import org.geektimes.di.function.ThrowableFunction;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.naming.*;
import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/23 22:24
 */
public class ClassicComponentContext implements ComponentContext {

    public static final String CONTEXT_NAME = ClassicComponentContext.class.getName();

    private static final Logger logger = Logger.getLogger(CONTEXT_NAME);

    private static final String COMPONENT_ENV_CONTEXT_NAME = "java:comp/env"; // jndi目录根节点

    private static ServletContext servletContext; // 请注意
    // 假设一个 Tomcat JVM 进程，三个 Web Apps，会不会相互冲突？（不会冲突）
    // static 字段是 JVM 缓存吗？（是 ClassLoader 缓存）

//    private static ApplicationContext applicationContext;

//    public void setApplicationContext(ApplicationContext applicationContext){
//        ComponentContext.applicationContext = applicationContext;
//        WebApplicationContextUtils.getRootWebApplicationContext()
//    }

    private Context envContext; // 环境上下文

    private Map<String, Object> componentsCache = new LinkedHashMap<>();

    private Map<Method, Object> preDestroyMethodCache = new LinkedHashMap<>();

    private ClassLoader envClassLoader;


    /**
     * 获取 ComponentContext
     *
     * @return
     */
    public static ClassicComponentContext getInstance() {
        return (ClassicComponentContext) servletContext.getAttribute(CONTEXT_NAME);
    }

    @Override
    public void init() {
        initClassLoader();
        // 1. 初始化环境变量 获取java:comp/env中的所有上下文
        initEnvContext();
        // 2. 实例化环境变量 (java:comp/env中定义的需要用到的), 仅仅是实例化 (new)
        instantiateComponents();
        // 3. 初始化这些对象
        initializeComponents();
        registerShutdownHook();
    }

    @Override
    public void destroy() throws RuntimeException {
        processPreDestroy(); // 执行被@PreDestroy标记的方法
        closeEnvContext(); // 关闭环境上下文
        clearCache(); // 清空相关缓存
    }

    /**
     * 通过名称进行依赖查找
     *
     * @param name
     * @param <C>
     * @return
     */
    @Override
    public <C> C getComponent(String name) {
        return (C) componentsCache.get(name);
    }

    @Override
    public List<String> getComponentNames() {
        return new ArrayList<>(componentsCache.keySet());
    }

    /**
     * 初始化上下文
     *
     * @throws RuntimeException
     */
    public void init(ServletContext servletContext) throws RuntimeException {
//        servletContext.log("初始化 java:comp/env 上下文成功!");
        servletContext.setAttribute(CONTEXT_NAME, this);
        ClassicComponentContext.servletContext = servletContext;
        this.init();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::processPreDestroy));
    }

    private void initClassLoader() {
        this.envClassLoader = servletContext.getClassLoader();
    }

    /**
     * 初始化组件（支持 Java 标准 Commons Annotation 生命周期）
     */
    private void initializeComponents() {
        componentsCache.values().forEach(this::initializeComponent);
    }

    /**
     * 初始化单个组件（支持 Java 标准 Commons Annotation 生命周期）
     * <ol>
     *  <li>注入阶段 - {@link Resource}</li>
     *  <li>初始阶段 - {@link PostConstruct}</li>
     *  <li>销毁阶段 - {@link PreDestroy}</li>
     * </ol>
     */
    private void initializeComponent(Object component) {
        Class<?> componentClass = component.getClass();
        // 1. 注入阶段
        injectComponent(component, componentClass);
        // 2. 寻找候选方法, 为后续的初始阶段和销毁阶段做准备
        List<Method> candidateMethods = findCandidateMethods(componentClass);
        // 3. 初始阶段
        processPostConstruct(component, candidateMethods);
        // 4. 销毁阶段(这个阶段并不会执行销毁方法,而是把这个方法放到一个map中,在destroy时执行)
        processPreDestroyMetadata(component, candidateMethods);
    }

    /**
     * 寻找候选方法
     * 1. 非static方法
     * 2. 方法参数个数 0
     *
     * @param componentClass
     * @return
     */
    private List<Method> findCandidateMethods(Class<?> componentClass) {
        return Stream.of(componentClass.getMethods())
                .filter(method -> {
                    int mods = method.getModifiers();
                    return !Modifier.isStatic(mods) // 非static
                            && method.getParameterCount() == 0; // 方法参数个数 0
                })
                .collect(Collectors.toList());
    }

    /**
     * 注入阶段
     *
     * @param component
     * @param componentClass
     */
    private void injectComponent(Object component, Class<?> componentClass) {
        Stream.of(componentClass.getDeclaredFields())
                .filter(field -> {
                    // 保留非static和被Resource注解标记的field
                    int mods = field.getModifiers();
                    return !Modifier.isStatic(mods) &&
                            field.isAnnotationPresent(Resource.class);
                })
                .forEach(field -> {
                    Resource resource = field.getAnnotation(Resource.class);
                    // 获取Resource注解中name定义的内容
                    String resourceName = resource.name();
                    Object injectedObj = lookupComponent(resourceName);
                    // 注入
                    field.setAccessible(true);
                    try {
                        field.set(component, injectedObj);
                    } catch (IllegalAccessException e) {
                    }
                });
    }


    /**
     * 执行被@PostConstruct标注的方法
     *
     * @param
     * @param candidateMethods
     */
    private void processPostConstruct(Object component, List<Method> candidateMethods) {
        candidateMethods.stream()
                .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                .forEach(method -> {
                    // 执行被@PostConstruct标记的方法
                    try {
                        method.invoke(component);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    /**
     * 销毁阶段
     * 并不会立刻执行这个方法,而是把它存入map中,在destroy时在执行
     */
    private void processPreDestroyMetadata(Object component, List<Method> candidateMethods) {
        candidateMethods.stream()
                .filter(method -> method.isAnnotationPresent(PreDestroy.class))
                .forEach(method -> {
                    preDestroyMethodCache.put(method, component);
                });
    }

    /**
     * 在销毁阶段 执行被@PreDestroy标记过得方法
     */
    private void processPreDestroy() {
        // 遍历preDestroyMethodCache, 并执行
        for (Method preDestroyMethod : preDestroyMethodCache.keySet()) {
            Object component = preDestroyMethodCache.remove(preDestroyMethod); // 从preDestroyMethodCache中移除, 防止重复执行
            ThrowableAction.execute(() -> preDestroyMethod.invoke(component));
        }
    }

    /**
     * 初始化 java:comp/env 上下文
     */
    private void initEnvContext() {
        if (this.envContext != null) return;
        Context context = null;
        try {
            context = new InitialContext();
            this.envContext = (Context) context.lookup(COMPONENT_ENV_CONTEXT_NAME);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            close(context);
        }
    }

    /**
     * 实例化子节点, 并放入componentsMap
     */
    protected void instantiateComponents() {
        // 列出根节点下所有的子节点名称
        // jndi和spring ioc有什么区别呢? spring允许动态配置
        List<String> componentsName = listAllComponentNames();
        // 通过依赖查找，实例化对象（ Tomcat BeanFactory setter 方法的执行，仅支持简单类型）
        // 这个时候的对象仅仅是new了一下,还没有初始化
        componentsName.forEach(item -> componentsCache.put(item, lookupComponent(item)));
    }

    /**
     * 根据路径搜索component
     *
     * @param item
     * @param <C>
     * @return
     */
    private <C> C lookupComponent(String item) {
        return executionInContext(ctx -> (C) ctx.lookup(item));
    }

    /**
     * 列出所有组件名
     *
     * @return
     */
    private List<String> listAllComponentNames() {
        return listAllComponentNames("/");
    }

    /**
     * 根据name列出组件名
     *
     * @param name
     * @return
     */
    private List<String> listAllComponentNames(String name) {
        return executionInContext(context -> {
            // 使用executionInContext的目的在于:
            // 不希望调用context.list(name)时候抛异常. 如果目录中没有节点了之后,就会抛出异常, 我们不希望显式的抛出异常
            NamingEnumeration<NameClassPair> enumeration = executionInContext(context, ctx -> ctx.list(name), true);
            // 目录 - context
            if (enumeration == null) { // 当前目录下没有子节点
                return Collections.emptyList();
            }
            List<String> fullNames = new LinkedList<>();
            while (enumeration.hasMoreElements()) { // 遍历当前节点中的子节点
                NameClassPair element = enumeration.nextElement();
                String className = element.getClassName();
                Class<?> targerClass = this.envClassLoader.loadClass(className);
                if (Context.class.isAssignableFrom(targerClass)) {
                    // 如果当前名称是目录（Context 实现类）的话，递归查找
                    fullNames.addAll(listAllComponentNames(element.getName()));
                } else {
                    // 非目录, 当前名称绑定目标类型的话，添加该名称到集合中
                    String fullName = name.startsWith("/") ? element.getName()
                            : name + "/" + element.getName();
                    fullNames.add(fullName);
                }
            }
            return fullNames;
        });
    }

    /**
     * 在 Context 中执行，通过指定 ThrowableFunction 返回计算结果
     *
     * @param function ThrowableFunction
     * @param <R>      返回结果类型
     * @return 返回
     * @see ThrowableFunction#apply(Object)
     */
    protected <R> R executionInContext(ThrowableFunction<Context, R> function) {
        return executionInContext(function, false);
    }

    /**
     * 在 Context 中执行，通过指定 ThrowableFunction 返回计算结果
     *
     * @param function ThrowableFunction
     * @param <R>      返回结果类型
     * @return 返回
     * @see ThrowableFunction#apply(Object)
     */
    protected <R> R executionInContext(ThrowableFunction<Context, R> function, boolean ignoredException) {
        return executionInContext(this.envContext, function, ignoredException);
    }

    private <R> R executionInContext(Context context, ThrowableFunction<Context, R> function, boolean ignoredException) {
        R result = null;
        try {
            result = ThrowableFunction.execute(context, function);
        } catch (Throwable e) {
            if (ignoredException) {
                logger.warning(e.getMessage());
            } else {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private void closeEnvContext() {
        close(this.envContext);
    }

    private void clearCache() {
        this.componentsCache.clear();
        this.preDestroyMethodCache.clear();
    }

    private static void close(Context context) {
        if (context != null) {
            ThrowableAction.execute(context::close);
        }
    }
}
