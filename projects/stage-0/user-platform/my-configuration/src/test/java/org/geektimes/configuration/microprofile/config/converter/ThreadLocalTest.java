package org.geektimes.configuration.microprofile.config.converter;

import org.junit.Test;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/26 10:52
 */
public class ThreadLocalTest {

    /**
     * 定义一个静态内部类, 初始化两个ThreadLocal变量
     */
    static class ResourceClass {
        public final static ThreadLocal<String> THREAD_NAME_RESOURCE = new ThreadLocal<>();

        public final static ThreadLocal<Integer> VALUE_RESOURCE = new ThreadLocal<>();
    }

    static class Setter {
        public void setThreadName(String name) {
            ResourceClass.THREAD_NAME_RESOURCE.set(name);
        }

        public void setThreadValue(int val) {
            ResourceClass.VALUE_RESOURCE.set(val);
        }
    }

    static class Display {
        public void display() {
            System.out.println(ResourceClass.THREAD_NAME_RESOURCE.get() +
                    ": " + ResourceClass.VALUE_RESOURCE.get());
        }
    }

    @Test
    public void testThreadLocal() {
        Setter setter = new Setter();
        Display display = new Display();
        for(int i = 0; i < 10; i++) {
            String threadName = "线程-" + i;
            int val = i;
            new Thread(() -> {
                try {
                    setter.setThreadName(threadName);
                    setter.setThreadValue(val);
                    display.display();
                } finally {
                    ResourceClass.THREAD_NAME_RESOURCE.remove();
                    ResourceClass.VALUE_RESOURCE.remove();
                }
            }).start();
        }
    }
}
