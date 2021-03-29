package org.geektimes.di.context;

import java.util.List;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/28 22:04
 */
public interface ComponentContext {

    /*
        生命周期方法
     */

    /**
     * 上下文初始化方法
     */
    void init();

    /**
     * 上下文销毁方法
     */
    void destroy();

    /*
        组件操作方法
     */

    /**
     * 根据组件名称获取组件上下文
     * @param name
     * @param <C>
     * @return
     */
    <C> C getComponent(String name);

    /**
     * 获取所有组件的名称
     * @return
     */
    List<String> getComponentNames();
}
