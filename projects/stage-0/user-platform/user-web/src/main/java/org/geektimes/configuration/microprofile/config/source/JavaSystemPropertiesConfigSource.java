package org.geektimes.configuration.microprofile.config.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JavaSystemPropertiesConfigSource implements ConfigSource {

    /**
     * Java 系统属性最好通过本地变量保存，使用 Map 保存，尽可能运行期不去调整
     * -Dapplication.name=user-web
     * Q. 为什么不用Properties properties来保存,而是用Map<String, String>来保存?
     * A. 因为getProperties方法中的super.get()是同步的, 当QPS很大的时候, 要取一个配置时, 性能会受到很大影响
     */
    private final Map<String, String> properties;

    public JavaSystemPropertiesConfigSource() {
        this.properties = new HashMap<>();
        for (String propertyName : System.getProperties().stringPropertyNames()) {
            this.properties.put(propertyName, System.getProperties().getProperty(propertyName));
        }
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return "Java System Properties";
    }
}
