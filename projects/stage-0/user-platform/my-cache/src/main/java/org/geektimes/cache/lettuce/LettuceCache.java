package org.geektimes.cache.lettuce;

import io.lettuce.core.api.sync.RedisCommands;
import org.geektimes.cache.AbstractCache;
import org.geektimes.cache.ExpirableEntry;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.io.*;
import java.util.Set;

/**
 * @author xuejz
 * @description
 * @Time 2021/4/15 09:58
 */
public class LettuceCache<K extends Serializable, V extends Serializable> extends AbstractCache<K, V> {

    private final RedisCommands<K, V> redisCommands;

    protected LettuceCache(CacheManager cacheManager, String cacheName, Configuration<K, V> configuration, RedisCommands<K, V> redisCommands) {
        super(cacheManager, cacheName, configuration);
        this.redisCommands = redisCommands;
    }

    @Override
    protected boolean containsEntry(K key) throws CacheException, ClassCastException {
        long keyNum = redisCommands.exists(key);
        return keyNum == 1;
    }

    @Override
    protected ExpirableEntry<K, V> getEntry(K key) throws CacheException, ClassCastException {
        V val = redisCommands.get(key);
        return ExpirableEntry.of(key, val);
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

}
