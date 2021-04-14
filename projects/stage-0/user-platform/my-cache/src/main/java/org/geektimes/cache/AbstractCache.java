/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.geektimes.cache;

import org.geektimes.cache.event.CacheEntryEventPublisher;
import org.geektimes.cache.integration.CompositeFallbackStorage;
import org.geektimes.cache.integration.FallbackStorage;
import org.geektimes.cache.processor.MutableEntryAdapter;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;
import javax.cache.expiry.Duration;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.cache.processor.MutableEntry;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.geektimes.cache.ExpirableEntry.requireKeyNotNull;
import static org.geektimes.cache.ExpirableEntry.requireValueNotNull;
import static org.geektimes.cache.configuration.ConfigurationUtils.immutableConfiguration;
import static org.geektimes.cache.configuration.ConfigurationUtils.mutableConfiguration;
import static org.geektimes.cache.event.GenericCacheEntryEvent.*;

/**
 * The abstract non-thread-safe implementation of {@link Cache}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private final CacheManager cacheManager;

    private final String cacheName;

    private final CompleteConfiguration<K, V> configuration;

    private final CacheLoader<K, V> cacheLoader;

    private final CacheWriter<K, V> cacheWriter;

    private final ExpiryPolicy expiryPolicy; // 过期策略

    private final FallbackStorage defaultFallbackStorage;

    private final CacheEntryEventPublisher entryEventPublisher; // Cache Event 发送器

    private final Executor executor;

    private volatile boolean closed = false;

    protected AbstractCache(CacheManager cacheManager, String cacheName, Configuration<K, V> configuration) {
        this.cacheManager = cacheManager;
        this.cacheName = cacheName;
        this.configuration = mutableConfiguration(configuration);
        this.defaultFallbackStorage = new CompositeFallbackStorage(cacheManager.getClassLoader());
        this.entryEventPublisher = new CacheEntryEventPublisher();
        this.cacheLoader = resolveCacheLoader(getConfiguration(), getClassLoader());
        this.cacheWriter = resolveCacheWriter(getConfiguration(), getClassLoader());
        this.expiryPolicy = resolveExpiryPolicy(this.configuration);
        this.executor = ForkJoinPool.commonPool();
        registerCacheEntryListenersFromConfiguration();
    }

    @Override
    public boolean containsKey(K key) {
        assertNotClosed();
        return containsEntry(key);
    }

    /**
     * Gets an entry from the cache.
     * <p>
     * If the cache is configured to use read-through, and get would return null
     * because the entry is missing from the cache, the Cache's {@link CacheLoader}
     * is called in an attempt to load the entry.
     * <p>
     * Current method calls the methods of {@link ExpiryPolicy}:
     * <ul>
     *     <li>No {@link ExpiryPolicy#getExpiryForCreation} ({@link #loadValue(Object, boolean) unless read-though caused a load)}</li>
     *     <li>Yes {@link ExpiryPolicy#getExpiryForAccess}</li>
     *     <li>No {@link ExpiryPolicy#getExpiryForUpdate}</li>
     * </ul>
     *
     * @param key the key whose associated value is to be returned
     * @param key the specified key
     * @return the element, or null, if it does not exist.
     * @throws IllegalStateException if the cache is {@link #isClosed()}
     * @throws NullPointerException  if the key is null
     * @throws CacheException        if there is a problem fetching the value
     * @throws ClassCastException    if the implementation is configured to perform
     *                               runtime-type-checking, and the key or value
     *                               types are incompatible with those that have been
     *                               configured for the {@link Cache}
     */
    @Override
    public V get(K key) {
        assertNotClosed();
        requireKeyNotNull(key);
        ExpirableEntry<K, V> entry = null;
        try {
            entry = getEntry(key);
            // 如果entry已经过期, 那么返回null
            if (handleExpiryPolicyForAccess(entry)) return null;
        } catch (Throwable e) {
            logger.severe(e.getMessage());
        }
        // If cache missing and read-through enabled, try to load value by {@link CacheLoader}
        if (entry == null && isReadThrough())
            return loadValue(key);
        return getValue(entry);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        // Keep the order of keys
        Map<K, V> result = new LinkedHashMap<>();
        for (K key : keys) {
            result.put(key, get(key));
        }
        return result;
    }

    @Override
    public V getAndPut(K key, V value) {
        Entry<K, V> oldEntry = getEntry(key);
        V oldValue = getValue(oldEntry);
        put(key, value);
        return oldValue;
    }

    @Override
    public V getAndRemove(K key) {
        Entry<K, V> oldEntry = getEntry(key);
        V oldValue = getValue(oldEntry);
        remove(key);
        return oldValue;
    }

    @Override
    public V getAndReplace(K key, V value) {
        Entry<K, V> oldEntry = getEntry(key);
        V oldValue = getValue(oldEntry);
        if (oldValue != null) {
            put(key, value);
        }
        return oldValue;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Current method calls the methods of {@link ExpiryPolicy}:
     * <ul>
     *     <li>Yes {@link ExpiryPolicy#getExpiryForCreation} (when the key is not associated with an existing value)</li>
     *     <li>No {@link ExpiryPolicy#getExpiryForAccess}</li>
     *     <li>Yes {@link ExpiryPolicy#getExpiryForUpdate} (when the key is associated with a loaded value and the value
     *     should be replaced)</li>
     * </ul>
     */
    @Override
    public void loadAll(Set<? extends K> keys, boolean replaceExistingValues,
                        CompletionListener completionListener) {
        assertNotClosed();
        // If no loader is configured for the cache, no objects will be loaded.
        if (!configuration.isReadThrough()) {
            // FIXME: The specification does not mention that
            // CompletionListener#onCompletion() method should be invoked or not.
            completionListener.onCompletion();
            return;
        }

        // Asynchronously loads the specified entries into the cache using the configured
        // CacheLoader for the given keys.
        CompletableFuture.runAsync(() -> {
            // Implementations may choose to load multiple keys from the provided Set in parallel.
            // Iteration however must not occur in parallel, thus allow for non-thread-safe Sets to be used.
            for (K key : keys) {
                // If an entry for a key already exists in the Cache, a value will be loaded
                // if and only if replaceExistingValues is true.
                V value = loadValue(key, false);
                if (replaceExistingValues) {
                    replace(key, value);
                } else {
                    put(key, value);
                }
            }
        }, executor).whenComplete((v, e) -> {
            // the CompletionListener may be null
            if (completionListener != null) {
                // completed exceptionally
                if (e instanceof Exception && e.getCause() instanceof Exception) {
                    completionListener.onException((Exception) e.getCause());
                } else {
                    completionListener.onCompletion();
                }
            }
        });
    }

    @Override
    public void put(K key, V value) {
        assertNotClosed();
        Entry<K, V> entry = null;
        try {
            if (!containsEntry(key)) {
                // entry不存在, 新建一个Entry再put
                entry = createAndPutEntry(key, value);
            } else {
                // entry存在, 直接更新
                entry = updateEntry(key, value);
            }
        } finally {
            writeEntryIfWriteThrough(entry);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        if (!containsKey(key)) {
            put(key, value);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(K key) {
        assertNotClosed();
        requireKeyNotNull(key);
        boolean removed = false;
        try {
            ExpirableEntry<K, V> oldEntry = removeEntry(key);
            removed = oldEntry != null;
            if (removed) {
                publishRemovedEvent(key, oldEntry.getValue());
            }
        } finally {
            deleteIfWriteThrough(key);
        }
        return removed;
    }

    @Override
    public boolean remove(K key, V oldValue) {
        if (containsKey(key) && Objects.equals(get(key), oldValue)) {
            remove(key);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void removeAll() {
        removeAll(keySet());
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        for (K key : keys) {
            remove(key);
        }
    }


    @Override
    public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
        MutableEntry<K, V> mutableEntry = MutableEntryAdapter.of(key, this);
        return entryProcessor.process(mutableEntry, arguments);
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
        Map<K, EntryProcessorResult<T>> resultMap = new LinkedHashMap<>();
        for (K key : keys) {
            resultMap.put(key, () -> invoke(key, entryProcessor, arguments));
        }
        return resultMap;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        requireValueNotNull(oldValue);
        if (containsKey(key) && Objects.equals(get(key), oldValue)) {
            put(key, newValue);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean replace(K key, V value) {
        if (containsKey(key)) {
            put(key, value);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        assertNotClosed();
        clearEntries();
        defaultFallbackStorage.destroy();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        assertNotClosed();
        List<Entry<K, V>> entrieList = new LinkedList<>();
        for (K key : keySet()) {
            V val = get(key);
            entrieList.add(ExpirableEntry.of(key, val));
        }
        return entrieList.iterator();
    }

    @Override
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
        if (!Configuration.class.isAssignableFrom(clazz)) {
            // clazz不是Configuration的子类
            throw new IllegalArgumentException("The class must be inherited of " + Configuration.class.getName());
        }
        return (C) immutableConfiguration(getConfiguration());
    }


    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        entryEventPublisher.registerCacheEntryListener(cacheEntryListenerConfiguration);
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        entryEventPublisher.deregisterCacheEntryListener(cacheEntryListenerConfiguration);
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return getCacheManager().unwrap(clazz);
    }

    @Override
    public final String getName() {
        return cacheName;
    }

    @Override
    public final CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    public final void close() {
        // Closing a Cache signals to the CacheManager that produced or owns the Cache
        // that it should no longer be managed.

        if (isClosed()) {
            return;
        }
        doClose();

        //  At this point in time the CacheManager:

        closed = true;
    }

    /**
     * Subclass could override this method.
     */
    protected void doClose() {
        // 为什么不设置为抽象方法?
    }

    @Override
    public final boolean isClosed() {
        return closed;
    }

    // Operations of CompleteConfiguration

    public CompleteConfiguration<K, V> getConfiguration() {
        return configuration;
    }

    protected final boolean isReadThrough() {
        return configuration.isReadThrough();
    }

    protected final boolean isWriteThrough() {
        return configuration.isWriteThrough();
    }

    protected final boolean isStatisticsEnabled() {
        return configuration.isStatisticsEnabled();
    }

    protected final boolean isManagementEnabled() {
        return configuration.isManagementEnabled();
    }

    // Operations of Cache.Entry and ExpirableEntry

    private Entry<K, V> createAndPutEntry(K key, V value) {
        ExpirableEntry<K, V> newEntry = createEntry(key, value); // 创建一个新的Entry
        if (handleExpiryPolicyForCreation(newEntry)) {
            // Entry已过期
            return null;
        }
        putEntry(newEntry); // 添加进Entry
        publishCreatedEvent(key, value); // 发布Entry创建事件
        return newEntry;
    }

    private ExpirableEntry<K, V> createEntry(K key, V value) {
        return ExpirableEntry.of(key, value);
    }

    private Entry<K, V> updateEntry(K key, V value) {
        ExpirableEntry<K, V> oldEntry = getEntry(key); // 获取旧的Entry
        V oldValue = oldEntry.getValue(); // 获取oldEntry的value
        // 更新oldEntry
        oldEntry.setValue(value);
        putEntry(oldEntry);
        publishUpdatedEvent(key, oldValue, value);
        if (handleExpiryPolicyForUpdate(oldEntry)) {
            return null;
        }
        return oldEntry;
    }

    protected abstract ExpirableEntry<K, V> getEntry(K key) throws CacheException, ClassCastException;

    protected abstract boolean containsEntry(K key) throws CacheException, ClassCastException;

    protected abstract void putEntry(ExpirableEntry<K, V> newEntry) throws CacheException, ClassCastException;

    protected abstract ExpirableEntry<K, V> removeEntry(K key) throws CacheException, ClassCastException;

    /**
     * Get all keys of {@link Cache.Entry} in the {@link Cache}
     *
     * @return the non-null read-only {@link Set}
     */
    protected abstract Set<K> keySet();

    protected abstract void clearEntries() throws CacheException;

    // Operations of CacheLoader and CacheWriter

    protected CacheLoader<K, V> getCacheLoader() {
        return this.cacheLoader;
    }

    protected CacheWriter<K, V> getCacheWriter() {
        return this.cacheWriter;
    }

    private CacheWriter<K, V> resolveCacheWriter(CompleteConfiguration<K, V> configuration, ClassLoader classLoader) {
        Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory = configuration.getCacheWriterFactory();
        CacheWriter<K, V> cacheWriter = null;
        if (cacheWriterFactory != null) {
            cacheWriter = (CacheWriter<K, V>) cacheWriterFactory.create();
        }

        if (cacheWriter == null) {
            cacheWriter = this.defaultFallbackStorage;
        }
        return cacheWriter;
    }

    private CacheLoader<K, V> resolveCacheLoader(CompleteConfiguration<K, V> configuration, ClassLoader classLoader) {
        Factory<CacheLoader<K, V>> cacheLoaderFactory = configuration.getCacheLoaderFactory();
        CacheLoader<K, V> cacheLoader = null;
        if (cacheLoaderFactory != null) {
            cacheLoader = cacheLoaderFactory.create();
        }

        if (cacheLoader == null) {
            cacheLoader = this.defaultFallbackStorage;
        }

        return cacheLoader;
    }

    private V loadValue(K key) {
        return getCacheLoader().load(key);
    }

    private V loadValue(K key, boolean storedEntry) {
        V value = loadValue(key);
        if (storedEntry && value != null) {
            put(key, value);
        }
        return value;
    }

    private void writeEntryIfWriteThrough(Entry<K, V> entry) {
        if (entry != null && isWriteThrough()) {
            getCacheWriter().write(entry);
        }
    }

    private void deleteIfWriteThrough(K key) {
        if (isWriteThrough()) {
            getCacheWriter().delete(key);
        }
    }

    // Operations of CacheEntryEvent and CacheEntryListenerConfiguration

    private void registerCacheEntryListenersFromConfiguration() {
        this.configuration.getCacheEntryListenerConfigurations()
                .forEach(this::registerCacheEntryListener);
    }

    private void publishCreatedEvent(K key, V value) {
        entryEventPublisher.publish(createdEvent(this, key, value));
    }

    private void publishUpdatedEvent(K key, V oldValue, V value) {
        entryEventPublisher.publish(updatedEvent(this, key, oldValue, value));
    }

    private void publishExpiredEvent(K key, V oldValue) {
        entryEventPublisher.publish(expiredEvent(this, key, oldValue));
    }

    private void publishRemovedEvent(K key, V oldValue) {
        entryEventPublisher.publish(removedEvent(this, key, oldValue));
    }

    // Operations of ExpiryPolicy and Duration

    private boolean handleExpiryPolicyForCreation(ExpirableEntry<K, V> newEntry) {
        return handleExpiryPolicy(newEntry, getExpiryForCreation(), false);
    }

    private boolean handleExpiryPolicyForAccess(ExpirableEntry<K, V> entry) {
        return handleExpiryPolicy(entry, getExpiryForAccess(), true);
    }

    private boolean handleExpiryPolicyForUpdate(ExpirableEntry<K, V> oldEntry) {
        return handleExpiryPolicy(oldEntry, getExpiryForUpdate(), true);
    }

    /**
     * Handle {@link ExpiryPolicy}
     * 过期Entry的处理策略
     *
     * @param entry               {@link ExpirableEntry}
     * @param duration            Creation : If a {@link Duration#ZERO} is returned the new Cache.Entry is considered
     *                            to be already expired and will not be added to the Cache.
     *                            Access or Update : If a {@link Duration#ZERO} is returned a Cache.Entry will be considered
     *                            immediately expired.
     *                            <code>null</code> will result in no change to the previously understood expiry
     *                            {@link Duration}.
     * @param removedExpiredEntry the expired {@link Cache.Entry} is removed or not.
     *                            If <code>true</code>, the {@link Cache.Entry} will be removed and publish an
     *                            {@link EventType#EXPIRED} of {@link CacheEntryEvent}.
     * @return <code>true</code> indicates the specified {@link Cache.Entry} should be expired.
     */
    private boolean handleExpiryPolicy(ExpirableEntry<K, V> entry, Duration duration, boolean removedExpiredEntry) {

        if (entry == null) return false;

        boolean isExpired = false;

        if (entry.isExpired()) {
            isExpired = true;
        } else if (duration != null) {
            if (duration.isZero() || entry.isExpired()) {
                // entry已经过期
                isExpired = true;
            } else {
                // entry没有过期, 重新计时
                long adjustedTime = duration.getAdjustedTime(System.currentTimeMillis());
                entry.setTimestamp(adjustedTime);
            }
        }

        if (removedExpiredEntry && isExpired) {
            // entry已经过期, 移除
            K key = entry.getKey();
            V val = entry.getValue();
            removeEntry(key);
            // publish 删除事件
            publishExpiredEvent(key, val);
        }
        return isExpired;
    }

    protected final Duration getExpiryForCreation() {
        return getDuration(expiryPolicy::getExpiryForCreation);
    }

    protected final Duration getExpiryForAccess() {
        return getDuration(expiryPolicy::getExpiryForAccess);
    }

    protected final Duration getExpiryForUpdate() {
        return getDuration(expiryPolicy::getExpiryForUpdate);
    }

    /**
     * 提供一个持续时间 ?
     *
     * @param durationSupplier
     * @return
     */
    private Duration getDuration(Supplier<Duration> durationSupplier) {
        Duration duration = null;
        try {
            duration = durationSupplier.get();
        } catch (Throwable ignore) {
            // default
            duration = Duration.ETERNAL;
        }
        return duration;
    }

    private ExpiryPolicy resolveExpiryPolicy(CompleteConfiguration<K, V> configuration) {
        Factory<ExpiryPolicy> expiryPolicyFactory = configuration.getExpiryPolicyFactory();
        if (expiryPolicyFactory == null) {
            // 默认永不过期策略
            expiryPolicyFactory = EternalExpiryPolicy::new;
        }
        return expiryPolicyFactory.create();
    }

    // Other Operations

    protected ClassLoader getClassLoader() {
        return getCacheManager().getClassLoader();
    }

    private void assertNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException("Current cache has been closed! No operation should be executed.");
        }
    }

    private static <K, V> V getValue(Entry<K, V> entry) {
        return entry == null ? null : entry.getValue();
    }

    protected static void assertNotNull(Object object, String source) {
        if (object == null) {
            throw new NullPointerException(source + " must not be null!");
        }
    }
}
