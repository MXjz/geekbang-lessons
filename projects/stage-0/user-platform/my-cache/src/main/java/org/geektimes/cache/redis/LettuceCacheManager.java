package org.geektimes.cache.redis;

import io.lettuce.core.RedisURI;
import org.geektimes.cache.AbstractCacheManager;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Properties;

/**
 * @author xuejz
 * @description
 * @Time 2021/4/15 09:58
 */
public class LettuceCacheManager extends AbstractCacheManager {

    private final RedisURI redisURI;

    public LettuceCacheManager(CachingProvider cachingProvider, URI uri, ClassLoader classLoader, Properties properties) {
        super(cachingProvider, uri, classLoader, properties);
        redisURI = RedisURI.create(uri);
    }

    @Override
    protected <K, V, C extends Configuration<K, V>> Cache doCreateCache(String cacheName, C configuration) {
        return new LettuceCache(this, cacheName, configuration, redisURI);
    }
}
