package org.geektimes.configuration.microprofile.config.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 基于Map的配置源的模板抽象类, 用于扩展
 * @author xuejz
 * @description
 * @Time 2021/3/20 13:56
 */
public abstract class MapBasedConfigSource implements ConfigSource {

    private final String name;

    private final int ordinal;

    private final Map<String, String> source;

    public MapBasedConfigSource(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
        this.source = getProperties();
    }

    @Override
    public Set<String> getPropertyNames() {
        return source.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return source.get(propertyName);
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final Map<String, String> getProperties() {
        Map<String, String> configData = new HashMap<>();
        try {
            prepareConfigData(configData);
        } catch (Throwable cause) {
            throw new IllegalStateException("准备配置数据发生错误", cause);
        }
        return Collections.unmodifiableMap(configData);
    }

    protected abstract void prepareConfigData(Map configData) throws Throwable;

    @Override
    public int getOrdinal() {
        return this.ordinal;
    }
}
