package org.geektimes.configuration.microprofile.config.source.servlet;

import org.geektimes.configuration.microprofile.config.source.MapBasedConfigSource;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/23 10:46
 */
public class ServletContextConfigSource extends MapBasedConfigSource {

    private final ServletContext servletContext;

    protected ServletContextConfigSource(ServletContext servletContext) {
        super("ServletContext Config Source", 600, false);
        this.servletContext = servletContext;
        super.setSource();
    }

    @Override
    protected void prepareConfigData(Map configData) throws Throwable {
        Enumeration<String> initParameterNames = servletContext.getInitParameterNames();
        while(initParameterNames.hasMoreElements()) {
            String initParameterName = initParameterNames.nextElement();
            configData.put(initParameterName, servletContext.getInitParameter(initParameterName));
        }
    }
}
