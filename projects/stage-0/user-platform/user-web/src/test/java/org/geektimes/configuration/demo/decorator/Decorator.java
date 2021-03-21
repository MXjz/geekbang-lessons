package org.geektimes.configuration.demo.decorator;

/**
 * 装饰器类
 * @author xuejz
 * @description
 * @Time 2021/3/20 23:45
 */
public class Decorator implements Component{

    private Component component;

    public Decorator(Component component) {
        this.component = component;
    }

    @Override
    public void biu() {
        this.component.biu();
    }
}
