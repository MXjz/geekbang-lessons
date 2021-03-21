package org.geektimes.configuration.microprofile.config.converter;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 17:21
 */
public class ByteConverter extends AbstractConverter<Byte> {
    @Override
    protected Byte doConvert(String value) {
        return Byte.valueOf(value);
    }
}
