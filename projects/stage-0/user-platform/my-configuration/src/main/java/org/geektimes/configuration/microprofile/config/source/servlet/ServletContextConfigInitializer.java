package org.geektimes.configuration.microprofile.config.source.servlet;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.geektimes.configuration.microprofile.config.source.MapBasedConfigSource;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

/**
 * 如何注册ServletContextListener到Servlet容器上?C
 * 实现ServletContainerInitializer接口, 当Servlet容器启动时, 会调用onStartUp方法
 * 调用servletContext.addListener()将ServletContextConfigInitializer注册到Servlet容器中
 * @author xuejz
 * @description
 * @Time 2021/3/23 10:50
 */
public class ServletContextConfigInitializer implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // 将配置源添加到ConfigSources中.
        ServletContext servletContext = servletContextEvent.getServletContext(); // 获取ServletContext
        ConfigProviderResolver configProviderResolver = ConfigProviderResolver.instance();
        ConfigBuilder configBuilder = configProviderResolver.getBuilder();
        ClassLoader classLoader = servletContext.getClassLoader();
        configBuilder.forClassLoader(classLoader); // 统一classloader
        // 默认configSource
        configBuilder.addDefaultSources();
        // 已发现的configSource
        configBuilder.addDiscoveredSources();
        // 通过代码把servletContextConfig扩展到configSource中
        //
        // 调用ServletContextConfigSource的构造函数时,
        // MapBasedConfigSource的构造函数会调用getProperties方法
        /**
         * 问题: new ServletContextConfigSource(servletContext)
         * 这段代码调用ServletContextConfigSource的构造函数
         * @see ServletContextConfigSource#ServletContextConfigSource(ServletContext)
         * 调用时会先去调用它的父类的构造方法
         * @see MapBasedConfigSource#MapBasedConfigSource(String, int)
         * MapBasedConfigSource的构造函数会调用getProperties方法, 进而调用抽象方法prepareConfigData
         * @see MapBasedConfigSource#prepareConfigData(Map)
         * 但是ServletContextConfigSource的prepareConfigData需要用到servletContext变量, 这时servletContext变量还没有初始化
         * 造成NullPointerException.
         * 如何解决 ? ?
         * 在执行父类的构造方法时, 增加一个标识符判断是否执行子类继承实现的抽象方法, 并在子类把servletContext初始化完成之后再去执行
         * 子类实现的抽象方法
         */
        configBuilder.withSources(new ServletContextConfigSource(servletContext));
        Config config = configBuilder.build(); // 获取Config
        // 注册config 并关联到当前的classLoader
        configProviderResolver.registerConfig(config, classLoader);
        // 那么怎么扩展可以让Config对象为my-web-mvc使用?
        servletContext.setAttribute("microconfig", config);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
//        ServletContext servletContext = servletContextEvent.getServletContext();
//        ClassLoader classLoader = servletContext.getClassLoader();
//        ConfigProviderResolver configProviderResolver = ConfigProviderResolver.instance();
//        Config config = configProviderResolver.getConfig(classLoader);
//        configProviderResolver.releaseConfig(config);
    }
}
