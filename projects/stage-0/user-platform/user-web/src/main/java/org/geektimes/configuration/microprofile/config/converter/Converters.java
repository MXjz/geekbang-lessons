package org.geektimes.configuration.microprofile.config.converter;

import org.eclipse.microprofile.config.spi.Converter;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 17:22
 */
public class Converters implements Iterable<Converter> {

    private static final int DEFAULT_PRIORITY = 100; // 默认优先级

    private final Map<Class<?>, PriorityQueue<PrioritizedConverter>> typedConverter = new HashMap<>(); // 键是要转换的类型, 值是转换类的优先级队列

    private ClassLoader classLoader;

    private boolean isAddedDiscoveredConverter = false;

    public Converters() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public Converters(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void addDiscoveredConverter() {
        if (isAddedDiscoveredConverter) {
            return;
        }
        // 添加已发现的Converter
        addConverters(ServiceLoader.load(Converter.class, classLoader));
        // 设置是否添加已发现的converter为true
        isAddedDiscoveredConverter = true;
    }


    public void addConverters(Converter... converters) {
        addConverters(Arrays.asList(converters));
    }

    public void addConverters(Iterable<Converter> converters) {
        converters.forEach(this::addConverter);
    }

    public List<Converter> getConverters(Class<?> convertedType) {
        PriorityQueue<PrioritizedConverter> converterQueue = typedConverter.get(convertedType);
        if (converterQueue == null || converterQueue.isEmpty()) {
            return Collections.emptyList();
        }
        List<Converter> converters = new LinkedList<>();
        for (PrioritizedConverter prioritizedConverter : converterQueue) {
            converters.add(prioritizedConverter);
        }
        return converters;
    }

    public void addConverter(Converter converter) {
        addConverter(converter, DEFAULT_PRIORITY); // 为converter赋予默认优先级
    }

    public void addConverter(Converter converter, int priority) {
        Class<?> convertedType = resolveConvertedType(converter); // 获取Converter要转换的类型
        addConverter(converter, priority, convertedType);
    }

    public void addConverter(Converter converter, int priority, Class<?> convertedType) {
        PriorityQueue priorityQueue = typedConverter.computeIfAbsent(convertedType, t -> new PriorityQueue<>());
        priorityQueue.offer(new PrioritizedConverter(converter, priority));
    }

    /**
     * 验证converter是否是接口及其是否是抽象类
     *
     * @param converter
     */
    private void assertConverter(Converter converter) {
        Class<?> converterClass = converter.getClass();
        if (converterClass.isInterface()) {
            // converter是接口
            throw new IllegalArgumentException("The implementation class of Converter must not be interface !");
        }
        if (Modifier.isAbstract(converterClass.getModifiers())) {
            // converter是抽象类
            throw new IllegalArgumentException("The implementation class of Converter must not be abstract!");
        }
    }

    /**
     * 解析Converter要转换的类型
     *
     * @param converter
     * @return
     */
    public Class<?> resolveConvertedType(Converter converter) {
        assertConverter(converter); // 判断converter是否是接口及其是否是抽象类
        Class<?> convertedType = null; // converter转换后的类型
        Class<?> converterClass = converter.getClass(); // 获取converter的类类型
        while (converterClass != null) {
            convertedType = resolveConvertedType(converterClass);
            if (convertedType != null) break;
            // 如果获取convertedType失败, 则去converterClass的父类中去找
            Type superclass = converterClass.getGenericSuperclass();
            if (superclass instanceof ParameterizedType) {
                convertedType = resolveConvertedType(superclass);
            }
            if (convertedType != null) break;
            converterClass = converterClass.getSuperclass(); // 获取converterClass的父类继续重复循环
        }
        return convertedType;
    }

    public Class<?> resolveConvertedType(Class<?> converterClass) {
        Class<?> convertedType = null;
        // 获取converterClass实现的泛型接口
        for (Type genericInterface : converterClass.getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType) {
                // 泛型接口类型是ParameterizedType, 可以认为实现的这个接口有参数类型
                convertedType = resolveConvertedType(genericInterface);
                if (convertedType != null) break;
            }
        }
        return convertedType;
    }

    /**
     * 根据converter实现的接口类型解析出它的参数类型
     *
     * @param type
     * @return
     */
    public Class<?> resolveConvertedType(Type type) {
        Class<?> convertedType = null;
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            if (pType.getRawType() instanceof Class) {
                Class<?> rawType = (Class<?>) pType.getRawType(); // 获取converter实现的接口的类型
                // 判断rawType这个类是否继承自Converter
                if (Converter.class.isAssignableFrom(rawType)) {
                    Type[] arguments = pType.getActualTypeArguments(); // 获取泛型接口的参数类型列表
                    if (arguments.length == 1 && arguments[0] instanceof Class) {
                        convertedType = (Class<?>) arguments[0];
                    }
                }
            }
        }
        return convertedType;
    }

    @Override
    public Iterator<Converter> iterator() {
        List<Converter> converters = new LinkedList<>();
        for (PriorityQueue<PrioritizedConverter> converterQueue : typedConverter.values()) {
            for (PrioritizedConverter prioritizedConverter : converterQueue) {
                converters.add(prioritizedConverter);
            }
        }
        return converters.iterator();
    }
}
