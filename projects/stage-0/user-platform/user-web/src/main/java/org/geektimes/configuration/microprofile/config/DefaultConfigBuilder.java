package org.geektimes.configuration.microprofile.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;
import org.geektimes.configuration.microprofile.config.converter.Converters;
import org.geektimes.configuration.microprofile.config.source.ConfigSources;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/21 19:36
 */
public class DefaultConfigBuilder implements ConfigBuilder {

    private ConfigSources configSources;

    private Converters converters;

    public DefaultConfigBuilder(ClassLoader classLoader) {
        this.configSources = new ConfigSources(classLoader);
        this.converters = new Converters(classLoader);
    }

    @Override
    public ConfigBuilder addDefaultSources() {
        configSources.addDefaultConfigSource();
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredSources() {
        configSources.addDiscoveredConfigSource();
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredConverters() {
        converters.addDiscoveredConverter();
        return this;
    }

    @Override
    public ConfigBuilder forClassLoader(ClassLoader loader) {
        converters.setClassLoader(loader);
        configSources.setClassLoader(loader);
        return this;
    }

    @Override
    public ConfigBuilder withSources(ConfigSource... sources) {
        configSources.addConfigSources(sources);
        return this;
    }

    @Override
    public ConfigBuilder withConverters(Converter<?>... converters) {
        this.converters.addConverters(converters);
        return this;
    }

    @Override
    public <T> ConfigBuilder withConverter(Class<T> type, int priority, Converter<T> converter) {
        this.converters.addConverter(converter, priority, type);
        return this;
    }

    @Override
    public Config build() {
        return new DefaultConfig(configSources, converters);
    }
}
