package org.geektimes.projects.user.management;

import org.geektimes.projects.user.domain.User;

import javax.management.MBeanInfo;
import javax.management.StandardMBean;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/15 22:59
 */
public class StandardMBeanDemo {

    public static void main(String[] args) throws Exception{
        // 将静态MBean接口 -> Dynamic MBean
        StandardMBean standardMBean = new StandardMBean(new UserManager(new User()), UserManagerMBean.class);

        MBeanInfo mBeanInfo = standardMBean.getMBeanInfo();

        System.out.println(mBeanInfo.toString());
    }
}
