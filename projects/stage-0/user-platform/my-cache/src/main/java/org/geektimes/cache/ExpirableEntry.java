package org.geektimes.cache;

import javax.cache.Cache;
import java.io.Serializable;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Expirable {@link Cache.Entry}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Map.Entry
 * @see Cache.Entry
 * @since 1.0
 */
public class ExpirableEntry<K, V> implements Cache.Entry<K, V>, Serializable {

    private static final long serialVersionUID = 4429955265057134784L;
    private final K key;

    private V value;

    private long timestamp;

    public ExpirableEntry(K key, V value) {
        requireKeyNotNull(key);
        this.key = key;
        setValue(value);
        this.timestamp = Long.MAX_VALUE;
    }

    public void setValue(V value) {
        requireValueNotNull(value);
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 判断Entry是否过期
     * @return
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= timestamp;
    }

    @Override
    public String toString() {
        return "ExpirableEntry{" +
                "key=" + key +
                ", value=" + value +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * 通过Map.Entry<K, V>创建ExpirableEntry对象
     * @param entry
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> ExpirableEntry<K, V> of(Map.Entry<K, V> entry) {
        return new ExpirableEntry<>(entry.getKey(), entry.getValue());
    }

    public static <K, V> ExpirableEntry<K, V> of(K key, V value) {
        return new ExpirableEntry<>(key, value);
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        T value = null;
        try {
            value = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    public static <K> void requireKeyNotNull(K key) {
        requireNonNull(key, "The key must not be null.");
    }

    public static <V> void requireValueNotNull(V value) {
        requireNonNull(value, "The value must not be null.");
    }

    public static <V> void requireOldValueNotNull(V oldValue) {
        requireNonNull(oldValue, "The oldValue must not be null.");
    }
}
