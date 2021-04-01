package org.geektimes.rest.entity;

import java.io.Serializable;

/**
 * @author xuejz
 * @description
 * @Time 2021/4/1 10:09
 */
public class TestEntity implements Serializable {
    private static final long serialVersionUID = 4303910943165037662L;

    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
