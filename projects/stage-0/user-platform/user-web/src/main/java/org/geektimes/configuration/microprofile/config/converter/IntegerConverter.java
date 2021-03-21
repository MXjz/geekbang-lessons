package org.geektimes.configuration.microprofile.config.converter;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 17:12
 */
public class IntegerConverter extends AbstractConverter<Integer> {
    @Override
    protected Integer doConvert(String value) {
        return Integer.valueOf(value);
    }
}
