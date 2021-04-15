package org.geektimes.cache.redis;

import org.geektimes.cache.AbstractCache;
import org.geektimes.cache.ExpirableEntry;
import org.geektimes.cache.redis.codec.KeyValCodec;
import redis.clients.jedis.Jedis;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Set;


public class JedisCache<K extends Serializable, V extends Serializable> extends AbstractCache<K, V> {

    private final Jedis jedis;

    private final KeyValCodec<K, V> codec;

    public JedisCache(CacheManager cacheManager, String cacheName,
                      Configuration<K, V> configuration, Jedis jedis) {
        super(cacheManager, cacheName, configuration);
        this.jedis = jedis;
        this.codec = new KeyValCodec<>(configuration.getKeyType(), configuration.getValueType());
    }

    @Override
    protected boolean containsEntry(K key) throws CacheException, ClassCastException {
        ByteBuffer keyByteBuffer = this.codec.encodeKey(key);
        return keyByteBuffer.hasArray() ? jedis.exists(keyByteBuffer.array()) : false;
    }

    @Override
    protected ExpirableEntry<K, V> getEntry(K key) throws CacheException, ClassCastException {
        ByteBuffer keyByteBuffer = this.codec.encodeKey(key);
        return keyByteBuffer.hasArray() ? getEntry(keyByteBuffer.array()) : null;
    }

    protected ExpirableEntry<K, V> getEntry(byte[] keyBytes) throws CacheException, ClassCastException {
        byte[] valueBytes = jedis.get(keyBytes);
        return ExpirableEntry.of(codec.decodeKey(keyBytes), codec.decodeValue(valueBytes));
    }

    @Override
    protected void putEntry(ExpirableEntry<K, V> entry) throws CacheException, ClassCastException {
//        byte[] keyBytes = serialize2Bytes(entry.getKey());
//        byte[] valueBytes = serialize2Bytes(entry.getValue());
        ByteBuffer keyByteBuffer = codec.encodeKey(entry.getKey());
        ByteBuffer valueByteBuffer = codec.encodeValue(entry.getValue());
        if (keyByteBuffer.hasArray() && valueByteBuffer.hasArray()) {
            jedis.set(keyByteBuffer.array(), valueByteBuffer.array());
        }
    }

    @Override
    protected ExpirableEntry<K, V> removeEntry(K key) throws CacheException, ClassCastException {
        ByteBuffer keyByteBuffer = codec.encodeKey(key);
        if (keyByteBuffer.hasArray()) {
            byte[] keyBytes = keyByteBuffer.array();
            ExpirableEntry<K, V> oldEntry = getEntry(keyBytes);
            jedis.del(keyBytes);
            return oldEntry;
        } else {
            return null;
        }
    }

    @Override
    protected void clearEntries() throws CacheException {
        // TODO
    }


    @Override
    protected Set<K> keySet() {
        // TODO
        return null;
    }

    @Override
    protected void doClose() {
        this.jedis.close();
    }

}
