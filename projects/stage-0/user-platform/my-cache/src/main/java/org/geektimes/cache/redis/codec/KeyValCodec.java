package org.geektimes.cache.redis.codec;

import io.lettuce.core.codec.RedisCodec;

import java.io.Serializable;
import java.nio.ByteBuffer;

import static org.geektimes.cache.redis.codec.DefaultCodecs.resolveCodec;

/**
 * @author xuejz
 * @description
 * @Time 2021/4/15 11:39
 */
public class KeyValCodec<K extends Serializable, V extends Serializable> implements RedisCodec<K, V> {

    private final Codec<K> keyCodec;

    private final Codec<V> valueCodec;

    public KeyValCodec(Class<?> keyType, Class<?> valueType) {
        this.keyCodec = resolveCodec(keyType);
        this.valueCodec = resolveCodec(valueType);
    }

    public K decodeKey(byte[] bytes) {
        return decodeKey(ByteBuffer.wrap(bytes));
    }

    public V decodeValue(byte[] bytes) {
        return decodeValue(ByteBuffer.wrap(bytes));
    }

    @Override
    public K decodeKey(ByteBuffer bytes) {
        return keyCodec.decode(bytes);
    }

    @Override
    public V decodeValue(ByteBuffer bytes) {
        return valueCodec.decode(bytes);
    }

    @Override
    public ByteBuffer encodeKey(K key) {
        return keyCodec.encode(key);
    }

    @Override
    public ByteBuffer encodeValue(V value) {
        return valueCodec.encode(value);
    }
}
