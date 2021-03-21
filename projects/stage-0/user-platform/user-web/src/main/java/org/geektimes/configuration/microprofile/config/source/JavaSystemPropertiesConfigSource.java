package org.geektimes.configuration.microprofile.config.source;

import java.util.Map;

public class JavaSystemPropertiesConfigSource extends MapBasedConfigSource {


    public JavaSystemPropertiesConfigSource(String name, int ordinal) {
        super("Java System Properties", 400);
    }

    /**
     * Java 系统属性最好通过本地变量保存，使用 Map 保存，尽可能运行期不去调整
     * -Dapplication.name=user-web
     */
    @Override
    protected void prepareConfigData(Map configData) throws Throwable {
        Map systemProperties = System.getProperties();
        configData.putAll(systemProperties);
    }
}
