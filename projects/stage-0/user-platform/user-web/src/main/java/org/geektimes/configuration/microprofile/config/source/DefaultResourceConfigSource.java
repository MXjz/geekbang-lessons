package org.geektimes.configuration.microprofile.config.source;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 读取META-INF/microprofile-config.properties配置文件中的配置
 * @author xuejz
 * @description
 * @Time 2021/3/20 14:31
 */
public class DefaultResourceConfigSource extends MapBasedConfigSource{

    private static final String resourceFileLocation = "META-INF/microprofile-config.properties";

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public DefaultResourceConfigSource(String name, int ordinal) {
        super("Default Resource Config", 100);
    }

    @Override
    protected void prepareConfigData(Map configData) throws Throwable {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(resourceFileLocation);
        if(resource == null) {
            logger.info("The default config file cannot be found in the classpath: " + resourceFileLocation);
            return;
        }
        try (InputStream inputStream = resource.openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            configData.putAll(properties);
        }
    }
}
