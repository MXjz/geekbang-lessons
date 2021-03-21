package org.geektimes.configuration.microprofile.config.converter;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 17:20
 */
public class BooleanConverter extends AbstractConverter<Boolean> {
    @Override
    protected Boolean doConvert(String value) {
        return Boolean.valueOf(value);
    }
}
