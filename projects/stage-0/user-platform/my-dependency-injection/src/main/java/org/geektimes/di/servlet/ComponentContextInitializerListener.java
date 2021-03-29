package org.geektimes.di.servlet;


import org.geektimes.di.context.ClassicComponentContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

/**
 * {@link ClassicComponentContext} 初始化器
 * ContextLoaderListener
 */
public class ComponentContextInitializerListener implements ServletContextListener {

    private ServletContext servletContext;

    private final Logger logger = Logger.getLogger(this.getClass().getName());


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContext = sce.getServletContext();
        ClassicComponentContext context = new ClassicComponentContext();
        context.init(servletContext);
        logger.info("ComponentContext init ok");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
//        ComponentContext context = ComponentContext.getInstance();
//        context.destroy();
    }

}
