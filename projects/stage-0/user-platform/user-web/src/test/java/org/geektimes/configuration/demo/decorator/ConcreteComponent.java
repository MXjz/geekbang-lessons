package org.geektimes.configuration.demo.decorator;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 23:44
 */
public class ConcreteComponent implements Component {
    @Override
    public void biu() {
        System.out.println("Concrete biu");
    }
}
