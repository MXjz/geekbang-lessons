package org.geektimes.cache.redis;

import org.geektimes.cache.AbstractCache;
import org.geektimes.cache.ExpirableEntry;
import redis.clients.jedis.Jedis;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.io.*;
import java.util.Set;

/**
 * @author xuejz
 * @description
 * @Time 2021/4/11 21:18
 */
public class JedisCache<K extends Serializable, V extends Serializable> extends AbstractCache<K, V> {

    private final Jedis jedis;

    protected JedisCache(CacheManager cacheManager, String cacheName, Configuration<K, V> configuration, Jedis jedis) {
        super(cacheManager, cacheName, configuration);
        this.jedis = jedis;
    }

    @Override
    protected ExpirableEntry<K, V> getEntry(K key) throws CacheException, ClassCastException {
        byte[] keyBytes = serialize(key); // 序列化key
        return getEntry(keyBytes);
    }

    protected ExpirableEntry<K, V> getEntry(byte[] key) throws CacheException, ClassCastException {
        byte[] valBytes = jedis.get(key);
        return ExpirableEntry.of(deserialize(key), deserialize(valBytes));
    }

    @Override
    protected boolean containsEntry(K key) throws CacheException, ClassCastException {
        return jedis.exists(serialize(key));
    }

    @Override
    protected void putEntry(ExpirableEntry<K, V> newEntry) throws CacheException, ClassCastException {
        byte[] keyBytes = serialize(newEntry.getKey());
        byte[] valBytes = serialize(newEntry.getValue());
        jedis.set(keyBytes, valBytes);
    }

    @Override
    protected ExpirableEntry<K, V> removeEntry(K key) throws CacheException, ClassCastException {
        byte[] keyBytes = serialize(key);
        ExpirableEntry<K, V> oldEntry = getEntry(keyBytes);
        jedis.del(keyBytes);
        return oldEntry;
    }

    @Override
    protected Set<K> keySet() {
        return null;
    }

    @Override
    protected void clearEntries() throws CacheException {
    }

    @Override
    protected void doClose() {
        this.jedis.close();
    }

    /**
     * 序列化对象
     *
     * @param key
     * @return
     * @throws CacheException
     */
    private byte[] serialize(Object key) throws CacheException {
        byte[] bytes = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(key);
            bytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new CacheException(e);
        }
        return bytes;
    }

    /**
     * 反序列化
     *
     * @param valBytes
     * @throws CacheException
     */
    private <T> T deserialize(byte[] valBytes) throws CacheException {
        if (valBytes == null) return null;
        T val = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(valBytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            val = (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new CacheException(e);
        }
        return val;
    }
}
