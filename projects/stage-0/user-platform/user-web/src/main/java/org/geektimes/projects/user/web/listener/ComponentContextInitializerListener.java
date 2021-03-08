package org.geektimes.projects.user.web.listener;

import org.geektimes.projects.user.context.ComponentContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ContextLoaderListener
 */
public class ComponentContextInitializerListener implements ServletContextListener {

    private ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContext = sce.getServletContext();
        ComponentContext context = new ComponentContext();
        servletContext.log("初始化 java:comp/env 上下文...");
        context.init(servletContext); // 初始化 java:comp/env 上下文, 方便后续获取bean/DBConnectionManager
        servletContext.log("初始化 java:comp/env 上下文完成");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ComponentContext context = ComponentContext.getInstance();
        context.destroy();
    }

}
