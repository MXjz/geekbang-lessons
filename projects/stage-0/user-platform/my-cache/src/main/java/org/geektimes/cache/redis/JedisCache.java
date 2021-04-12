package org.geektimes.cache.redis;

import org.geektimes.cache.AbstractCache;
import redis.clients.jedis.Jedis;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.io.*;
import java.util.Iterator;

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
    protected V doGet(K key) throws CacheException, ClassCastException {
        byte[] keyBytes = serialize(key);
        return doGet(keyBytes);
    }

    protected V doGet(byte[] key) throws CacheException, ClassCastException {
        byte[] valBytes = jedis.get(key);
        return valBytes == null ? null : deserialize(valBytes);
    }

    @Override
    protected V doPut(K key, V value) throws CacheException, ClassCastException {
        byte[] keyBytes = serialize(key);
        byte[] valBytes = serialize(value);
        V oldVal = doGet(key);
        jedis.set(keyBytes, valBytes);
        return oldVal;
    }

    @Override
    protected V doRemove(K key) throws CacheException, ClassCastException {
        byte[] keyBytes = serialize(key);
        V oldVal = doGet(key);
        jedis.del(keyBytes);
        return oldVal;
    }

    @Override
    protected void doClear() throws CacheException {

    }

    @Override
    protected void doClose() {
        this.jedis.close();
    }

    @Override
    protected Iterator<Entry<K, V>> newIterator() {
        return null;
    }

    /**
     * 序列化对象
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
     * @param valBytes
     * @throws CacheException
     */
    private V deserialize(byte[] valBytes) throws CacheException {
        V val = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(valBytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            val = (V) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new CacheException(e);
        }
        return val;
    }
}
