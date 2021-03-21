package org.geektimes.configuration.demo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 23:13
 */
public class TestGetGenericIntefaces {

    private class Food {
        String foodName;
    }

    private interface Eat<T> {
        void eat(T things);
    }

    private interface Run {
        void run();
    }

    private class Dog implements Eat<Food>, Run {
        @Override
        public void run() {
            System.out.println("Dog running");
        }

        @Override
        public void eat(Food things) {
            System.out.println("Dog eat " + things.foodName);
        }
    }

    public static void main(String[] args) {
        Class<?> dogClass = Dog.class;

        // 如果不给Eat加泛型,则下面两个方法获得的数据是一样的
        // 但是如果给Eat加上泛型之后, 则有一些变化:
        /**
         * clazz.getGenericInterfaces(); 生成的数组里有一个接口类型为：
         * @see ParameterizedType
         * ParameterizedType继承自Type 而ParameterizedType 和 Type 有什么区别呢？
         * ParameterizedType表示参数化类型
         * 例如 Collection<String>, ParameterizedType相对于Type新增了三个接口方法：
         * @see ParameterizedType#getActualTypeArguments() 获取泛型中的实际类型参数 - String
         * @see ParameterizedType#getRawType() - Collection
         * @see ParameterizedType#getOwnerType() - 调用Collection<String>的类型 吧
         */
        //Class<?>[] interfaces = dogClass.getInterfaces();
        Type[] genericInterfaces = dogClass.getGenericInterfaces();
        for(Type implInterface : genericInterfaces) {
            if (implInterface instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) implInterface;
                if(pType.getRawType() instanceof Class<?>) {
                    Class<?> rawType = (Class<?>) pType.getRawType();
                    Type[] actualTypeArguments = pType.getActualTypeArguments();
                    Type ownerType = pType.getOwnerType();
                }
            }
        }
    }

}
