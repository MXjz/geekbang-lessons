package org.geektimes.projects.user.web.listener;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.management.UserManager;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.management.ManagementFactory;

/**
 * 初始化JMX监听器
 * @author xuejz
 * @description
 * @Time 2021/3/16 23:57
 */
public class JMXInitializerListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        // 为 UserMXBean 定义 ObjectName
        try {
            ObjectName objectName = new ObjectName("jolokia:name=UserManager");
            // 创建 UserMBean 实例
            User user = new User();
            mBeanServer.registerMBean(createUserMBean(user), objectName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object createUserMBean(User user) throws Exception {
        return new UserManager(user);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
