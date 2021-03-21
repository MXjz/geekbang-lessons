package org.geektimes.configuration.microprofile.config.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Map;
import java.util.Set;

/**
 * 系统变量配置源
 * @author xuejz
 * @description
 * @Time 2021/3/17 21:13
 */
public class SystemEnvConfigSource extends MapBasedConfigSource {

    public SystemEnvConfigSource(String name, int ordinal) {
        super("OS Envirnment Config", 300);
    }

    @Override
    protected void prepareConfigData(Map configData) throws Throwable {
        configData.putAll(System.getenv());
    }
}
