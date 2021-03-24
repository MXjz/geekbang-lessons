package org.geektimes.configuration.microprofile.config.source.servlet;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/23 16:21
 */
public class ServletConfigInitializer implements ServletContainerInitializer {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext servletContext) throws ServletException {
        // 增加 ServletContextListener
        servletContext.addListener(ServletContextConfigInitializer.class);
        logger.info("ServletContextConfigInitializer ok");
    }
}
