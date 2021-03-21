package org.geektimes.configuration.microprofile.config.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Comparator;

/**
 * ConfigSource优先级比较器 - 单例
 * @author xuejz
 * @description
 * @Time 2021/3/20 15:03
 */
public class ConfigSourceOrdinalComparator implements Comparator<ConfigSource> {

    public static final Comparator<ConfigSource> INSTANCE = new ConfigSourceOrdinalComparator();

    private ConfigSourceOrdinalComparator() {
    }

    @Override
    public int compare(ConfigSource o1, ConfigSource o2) {
        // o1.ordinal < o2.ordinal 返回 -1
        // o1.ordinal = o2.ordinal 返回 0
        // o1.ordinal > o2.ordinal 返回 1
        // 返回-1时 第一个参数放前面
        // ConfigSource 的 ordinal越大 优先级越高, 应该让ordinal大的元素排前面,所以compare的参数应该是o2,o1
        return Integer.compare(o2.getOrdinal(), o1.getOrdinal());
    }
}
