package org.geektimes.projects.user.management;

/**
 * MBean 接口描述
 * @author xuejz
 * @description
 * @Time 2021/3/14 21:26
 */
public interface UserManagerMBean {

    // MBeanAttributeInfo
    Long getId();

    void setId(Long id);

    String getName();

    void setName(String name);

    String getPassword();

    void setPassword(String password);

    String getEmail();

    void setEmail(String email);

    String getPhoneNumber();

    void setPhoneNumber(String phoneNumber);

    // MBeanOperationInfo
    String toString();
}
