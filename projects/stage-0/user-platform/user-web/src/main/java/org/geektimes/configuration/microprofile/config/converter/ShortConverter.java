package org.geektimes.configuration.microprofile.config.converter;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 17:19
 */
public class ShortConverter extends AbstractConverter<Short> {
    @Override
    protected Short doConvert(String value) {
        return Short.valueOf(value);
    }
}
