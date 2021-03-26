package org.geektimes.configuration.microprofile.config.servlet.listener;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * 当一个servlet请求发送过来时, 或触发ServletRequestListener的requestInitialized方法
 * 这个时候可以把Config放到ThreadLocal中, 并在该线程中用到Config
 * @author xuejz
 * @description
 * @Time 2021/3/25 23:40
 */
public class ConfigServletRequestListener implements ServletRequestListener {

    private static final ThreadLocal<Config> configThreadLocal = new ThreadLocal<>();

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // 防止oom
        configThreadLocal.remove();
    }

    public static Config getConfig() {
        return configThreadLocal.get();
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        // 从ServletRequestEvent中获取ServletContext 和 从ServletRequest中获取ServletContext有什么不同的?
        //
        ServletRequest servletRequest = sre.getServletRequest();
        //ServletContext servletContext = sre.getServletContext();
        ServletContext servletContext = servletRequest.getServletContext();
        ClassLoader classLoader = servletContext.getClassLoader(); // 获取Se rvletContext的ClassLoader
        Config config = ConfigProviderResolver.instance().getConfig(classLoader); // 获取Config
        configThreadLocal.set(config);
    }
}
