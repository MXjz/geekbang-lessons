package org.geektimes.configuration.microprofile.config.converter;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 17:21
 */
public class FloatConverter extends AbstractConverter<Float> {
    @Override
    protected Float doConvert(String value) {
        return Float.valueOf(value);
    }
}
