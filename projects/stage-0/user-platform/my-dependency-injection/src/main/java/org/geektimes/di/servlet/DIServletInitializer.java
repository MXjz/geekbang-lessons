package org.geektimes.di.servlet;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/23 22:52
 */
public class DIServletInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        servletContext.addListener(ComponentContextInitializerListener.class);
    }
}
