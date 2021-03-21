package org.geektimes.configuration.microprofile.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultConfigProviderResolver extends ConfigProviderResolver {

    private ConcurrentMap<ClassLoader, Config> configsRepository = new ConcurrentHashMap<>();

    @Override
    public Config getConfig() {
        return getConfig(null);
    }

    @Override
    public Config getConfig(ClassLoader loader) {
        return configsRepository.computeIfAbsent(loader, this::newConfig);
    }

    private Config newConfig(ClassLoader classLoader) {
        return newConfigBuilder(classLoader).build();
    }

    private ConfigBuilder newConfigBuilder(ClassLoader classLoader) {
        return new DefaultConfigBuilder(resolveClassLoader(classLoader));
    }

    private ClassLoader resolveClassLoader(ClassLoader classLoader) {
        // this.getClass().getClassLoader() 和 Thread.currentThread().getContextClassLoader() 区别?
        return classLoader == null ? this.getClass().getClassLoader() : classLoader;
    }

    @Override
    public ConfigBuilder getBuilder() {
        return newConfigBuilder(null);
    }

    @Override
    public void registerConfig(Config config, ClassLoader classLoader) {
        configsRepository.put(classLoader, config);
    }

    @Override
    public void releaseConfig(Config config) {
        List<ClassLoader> targetKeys = new LinkedList<>();
        for (Map.Entry<ClassLoader, Config> classLoaderConfigEntry : configsRepository.entrySet()) {
            if (Objects.equals(config, classLoaderConfigEntry.getValue())) {
                targetKeys.add(classLoaderConfigEntry.getKey());
            }
        }
        targetKeys.forEach(configsRepository::remove);
    }
}
