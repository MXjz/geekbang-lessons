package org.geektimes.configuration.microprofile.config.converter;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 17:22
 */
public class DoubleConverter extends AbstractConverter<Double> {
    @Override
    protected Double doConvert(String value) {
        return Double.valueOf(value);
    }
}
