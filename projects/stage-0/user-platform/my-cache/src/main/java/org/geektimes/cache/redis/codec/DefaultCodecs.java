package org.geektimes.cache.redis.codec;

import javax.cache.CacheException;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * @author xuejz
 * @description
 * @Time 2021/4/15 16:17
 */
public class DefaultCodecs {

    public static Codec resolveCodec(Class clazz) {
        if (clazz == String.class) {
            return new StringCodec();
        } else if (clazz == Double.class) {
            return new DoubleCodec();
        } else if (clazz == Float.class) {
            return new FloatCodec();
        } else if (clazz == Long.class) {
            return new LongCodec();
        } else if (clazz == Integer.class) {
            return new IntegerCodec();
        } else if (clazz == Short.class) {
            return new ShortCodec();
        } else if (clazz == Byte.class) {
            return new ByteCodec();
        } else if (clazz == Character.class) {
            return new CharacterCodec();
        } else if (clazz == Boolean.class) {
            return new BooleanCodec();
        } else {
            return new DefaultCodec<>();
        }

    }

    public static class StringCodec implements Codec<String> {
        @Override
        public ByteBuffer encode(String s) {
            return _encode(s);
        }

        @Override
        public String decode(ByteBuffer byteBuffer) {
            return _decode(byteBuffer);
        }
    }

    public static class DoubleCodec implements Codec<Double> {

        @Override
        public Double decode(ByteBuffer buffer) {
            return Double.valueOf(_decode(buffer));
        }

        @Override
        public ByteBuffer encode(Double value) {
            return _encode(String.valueOf(value));
        }
    }

    public static class FloatCodec implements Codec<Float> {

        @Override
        public Float decode(ByteBuffer buffer) {
            return Float.valueOf(_decode(buffer));
        }

        @Override
        public ByteBuffer encode(Float value) {
            return _encode(String.valueOf(value));
        }
    }

    public static class LongCodec implements Codec<Long> {

        @Override
        public Long decode(ByteBuffer buffer) {
            return Long.valueOf(_decode(buffer));
        }

        @Override
        public ByteBuffer encode(Long value) {
            return _encode(String.valueOf(value));
        }
    }

    public static class IntegerCodec implements Codec<Integer> {

        @Override
        public Integer decode(ByteBuffer buffer) {
            return Integer.valueOf(_decode(buffer));
        }

        @Override
        public ByteBuffer encode(Integer value) {
            return _encode(String.valueOf(value));
        }
    }

    public static class ShortCodec implements Codec<Short> {
        @Override
        public Short decode(ByteBuffer buffer) {
            return Short.valueOf(_decode(buffer));
        }

        @Override
        public ByteBuffer encode(Short value) {
            return _encode(String.valueOf(value));
        }
    }

    public static class ByteCodec implements Codec<Byte> {

        @Override
        public Byte decode(ByteBuffer buffer) {
            return Byte.valueOf(_decode(buffer));
        }

        @Override
        public ByteBuffer encode(Byte value) {
            return _encode(String.valueOf(value));
        }
    }

    public static class CharacterCodec implements Codec<Character> {
        @Override
        public Character decode(ByteBuffer buffer) {
            return _decode(buffer).charAt(0);
        }

        @Override
        public ByteBuffer encode(Character value) {
            return _encode(String.valueOf(value));
        }
    }

    public static class BooleanCodec implements Codec<Boolean> {
        @Override
        public Boolean decode(ByteBuffer buffer) {
            return Boolean.valueOf(_decode(buffer));
        }

        @Override
        public ByteBuffer encode(Boolean value) {
            return _encode(String.valueOf(value));
        }
    }

    public static class DefaultCodec<T extends Serializable> implements Codec<T> {
        @Override
        public T decode(ByteBuffer buffer) {
            return buffer.hasArray() ? deserialize(buffer.array()) : null;
        }

        @Override
        public ByteBuffer encode(T value) {
            return ByteBuffer.wrap(serialize(value));
        }

        private byte[] serialize(Object value) throws CacheException {
            byte[] bytes = null;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
            ) {
                // Key -> byte[]
                objectOutputStream.writeObject(value);
                bytes = outputStream.toByteArray();
            } catch (IOException e) {
                throw new CacheException(e);
            }
            return bytes;
        }

        private T deserialize(byte[] bytes) throws CacheException {
            if (bytes == null) {
                return null;
            }
            T value = null;
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                 ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
            ) {
                // byte[] -> Value
                value = (T) objectInputStream.readObject();
            } catch (Exception e) {
                throw new CacheException(e);
            }
            return value;
        }
    }

}
