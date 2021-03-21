package org.geektimes.configuration.microprofile.config.converter;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/20 17:18
 */
public class StringConverter extends AbstractConverter<String>{
    @Override
    protected String doConvert(String value) {
        return value;
    }
}
