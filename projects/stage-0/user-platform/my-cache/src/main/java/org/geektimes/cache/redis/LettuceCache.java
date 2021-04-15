package org.geektimes.cache.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.geektimes.cache.AbstractCache;
import org.geektimes.cache.ExpirableEntry;
import org.geektimes.cache.redis.codec.KeyValCodec;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.io.Serializable;
import java.util.Set;

/**
 * @author xuejz
 * @description
 * @Time 2021/4/15 09:58
 */
public class LettuceCache<K extends Serializable, V extends Serializable> extends AbstractCache<K, V> {

    private final RedisCommands<K, V> redisCommands;

    private final RedisClient redisClient;

    private final StatefulRedisConnection<K, V> connection;

    protected LettuceCache(CacheManager cacheManager, String cacheName, Configuration<K, V> configuration, RedisURI redisURI) {
        super(cacheManager, cacheName, configuration);
        redisClient = RedisClient.create(redisURI);
        connection = redisClient.connect(new KeyValCodec<>(configuration.getKeyType(), configuration.getValueType()));     // <3> 创建线程安全的连接
        redisCommands = connection.sync();                // <4> 创建同步命令
    }

    @Override
    protected boolean containsEntry(K key) throws CacheException, ClassCastException {
        long keyNum = redisCommands.exists(key);
        return keyNum == 1;
    }

    @Override
    protected ExpirableEntry<K, V> getEntry(K key) throws CacheException, ClassCastException {
        V val = redisCommands.get(key);
        return val == null ? null : ExpirableEntry.of(key, val);
    }

    @Override
    protected void putEntry(ExpirableEntry<K, V> entry) throws CacheException, ClassCastException {
        redisCommands.set(entry.getKey(), entry.getValue());
    }

    @Override
    protected ExpirableEntry<K, V> removeEntry(K key) throws CacheException, ClassCastException {
        V oldVal = redisCommands.get(key);
        long delNum = redisCommands.del(key);
        return delNum == 1 ? ExpirableEntry.of(key, oldVal) : null;
    }

    @Override
    protected void clearEntries() throws CacheException {
        // todo
    }

    @Override
    protected Set<K> keySet() {
        // todo
        return null;
    }

    @Override
    protected void doClose() {
        connection.close();   // <5> 关闭连接
        redisClient.shutdown();  // <6> 关闭客户端
    }
}
