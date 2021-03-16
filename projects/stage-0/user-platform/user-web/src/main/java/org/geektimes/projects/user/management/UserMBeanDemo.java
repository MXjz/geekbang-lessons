package org.geektimes.projects.user.management;

import org.geektimes.projects.user.domain.User;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/15 22:08
 */
public class UserMBeanDemo {

    public static void main(String[] args) throws Exception {
        //Introspector.testCompliance(UserManager.class);
        // 获取平台MBeanServer
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        // 为UserMXBean定义ObjectName
        ObjectName objectName = new ObjectName("org.geektimes.projects.user.management:type=User");
        // 创建UserMBean实例
        User user = new User();
        mBeanServer.registerMBean(createUserMXBean(user), objectName);
        while(true) {
            Thread.sleep(2000);
            System.out.println(user);
        }
    }

    private static Object createUserMXBean(User user) {
        return new UserManager(user);
    }
}
