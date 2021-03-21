package org.geektimes.configuration.microprofile.config.converter;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 17:19
 */
public class LongConverter extends AbstractConverter<Long> {
    @Override
    protected Long doConvert(String value) {
        return Long.valueOf(value);
    }
}
