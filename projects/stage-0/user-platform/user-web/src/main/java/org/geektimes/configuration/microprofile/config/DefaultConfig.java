package org.geektimes.configuration.microprofile.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;
import org.geektimes.configuration.microprofile.config.converter.Converters;
import org.geektimes.configuration.microprofile.config.source.ConfigSources;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.StreamSupport.stream;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/21 21:29
 */
public class DefaultConfig implements Config {

    private ConfigSources configSources;

    private Converters converters;

    public DefaultConfig(ConfigSources configSources, Converters converters) {
        this.configSources = configSources;
        this.converters = converters;
    }

    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        // 遍历configSources, 调用configSource的getValue 获取到String 类型的value
        String propertyValue = getPropertyValue(propertyName);
        Converter<T> converter = doGetConverter(propertyType); // 获取对应的converter
        return converter == null ? null : converter.convert(propertyValue);
    }

    /**
     * 根据propertyType获取指定的converter
     *
     * @param propertyType
     * @param <T>
     * @return
     */
    protected <T> Converter<T> doGetConverter(Class<T> propertyType) {
        List<Converter> converters = this.converters.getConverters(propertyType);
        return converters.isEmpty() ? null : converters.get(0);
    }

    /**
     * 获取String类型的property value, 后续会通过converter对其进行类型转换
     *
     * @param propertyName
     * @return
     */
    protected String getPropertyValue(String propertyName) {
        // 遍历configSources, 调用configSource的getValue 获取到String 类型的value
        String propertyValue = null;
        for (ConfigSource configSource : configSources) {
            propertyValue = configSource.getValue(propertyName);
            if (propertyValue != null) {
                break;
            }
        }
        return propertyValue;
    }

    @Override
    public ConfigValue getConfigValue(String propertyName) {
        return null;
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        T value = getValue(propertyName, propertyType);
        return Optional.ofNullable(value);
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return stream(configSources.spliterator(), false)
                .map(ConfigSource::getPropertyNames)
                .collect(LinkedHashSet::new, Set::addAll, Set::addAll);
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return configSources;
    }

    @Override
    public <T> Optional<Converter<T>> getConverter(Class<T> forType) {
        Converter<T> converter = doGetConverter(forType);
        return converter == null ? Optional.empty() : Optional.of(converter);
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return null;
    }
}
