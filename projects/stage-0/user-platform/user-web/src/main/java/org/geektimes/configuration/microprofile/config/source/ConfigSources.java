package org.geektimes.configuration.microprofile.config.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 15:59
 */
public class ConfigSources implements Iterable<ConfigSource> {

    private boolean isAddedDefaultConfigSource; // 是否已经添加了默认配置源

    private boolean isAddedDiscoveredConfigSource; // 是否已添加了发现的配置源

    private List<ConfigSource> configSourceList = new ArrayList<>(); // 配置源列表

    private ClassLoader classLoader; // 类加载器, 用处?

    public ConfigSources(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 添加默认配置源
     */
    public void addDefaultConfigSource() {
        if (isAddedDefaultConfigSource) {
            // 已添加默认配置源, 直接返回
            return;
        }
        // 默认配置源只有以下三个:
        // 1. DefaultResourceConfigSource
        // 2. JavaSystemPropertiesConfigSource
        // 3. SystemEnvConfigSource
        addConfigSources(DefaultResourceConfigSource.class,
                JavaSystemPropertiesConfigSource.class,
                SystemEnvConfigSource.class);
    }

    /**
     * 添加发现的配置源
     */
    public void addDiscoveredConfigSource() {
        if (isAddedDiscoveredConfigSource) {
            // 已添加发现的配置源, 返回
            return;
        }
        addConfigSources(ServiceLoader.load(ConfigSource.class, classLoader));
        isAddedDiscoveredConfigSource = true;
    }

    /**
     * 主要是addDefaultConfigSource方法中在调用它
     * 需要在这个方法中对ConfigSource实例化
     * 其他地方几乎不使用
     * @param defaultConfigSources
     */
    public void addConfigSources(Class<? extends ConfigSource>... defaultConfigSources) {
        addConfigSources(Stream.of(defaultConfigSources)
                .map(this::newInstance)
                .collect(Collectors.toList()));
        //.toArray(ConfigSource[]::new); 是生成的ConfigSource数组
        //.collect(Collections.toList()) 是生成的List<ConfigSource>列表
        // 那如果不给.toArray 加ConfigSource[]::new这个参数生成的是什么样的数据?
    }

    public void addConfigSources(ConfigSource... configSourceArr) {
        addConfigSources(Arrays.asList(configSourceArr));
    }

    /**
     * 为什么不用List而是Iterable?
     *
     * @param configSourceList
     */
    public void addConfigSources(Iterable<ConfigSource> configSourceList) {
        configSourceList.forEach(this.configSourceList::add);
        this.configSourceList.sort(ConfigSourceOrdinalComparator.INSTANCE);
    }


    /**
     * 实例化configSource
     *
     * @param configSource
     * @return
     */
    private ConfigSource newInstance(Class<? extends ConfigSource> configSource) {
        ConfigSource instance;
        try {
            instance = configSource.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("实例化ConfigSource失败", e);
        }
        return instance;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public boolean isAddedDefaultConfigSource() {
        return isAddedDefaultConfigSource;
    }

    public boolean isAddedDiscoveredConfigSource() {
        return isAddedDiscoveredConfigSource;
    }

    @Override
    public Iterator<ConfigSource> iterator() {
        return configSourceList.iterator();
    }
}
