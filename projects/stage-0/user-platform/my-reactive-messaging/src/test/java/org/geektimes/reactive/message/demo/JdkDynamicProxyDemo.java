package org.geektimes.reactive.message.demo;

import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/30 11:15
 */
public class JdkDynamicProxyDemo {

    static class OutgoingMethodInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Type returnType = method.getGenericReturnType(); // 获取代理方法的返回类型
            if (returnType instanceof Class) { // 直接类型

            } else if (returnType instanceof ParameterizedType) { // 参数类型
                ParameterizedType ptype = (ParameterizedType) returnType;
                Type rawType = ptype.getRawType(); // 泛型的包装类型
                Type argType = ptype.getActualTypeArguments()[0]; // 泛型的参数类型
                if(rawType instanceof Class) {
                    Class rawReturnType = (Class) rawType;
                    if(Publisher.class.isAssignableFrom(rawReturnType)) {
                        // rawReturnType是Publisher的子类
                    }
                }
            }
            return null;
        }
    }
}
