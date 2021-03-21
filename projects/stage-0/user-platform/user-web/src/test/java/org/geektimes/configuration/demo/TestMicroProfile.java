package org.geektimes.configuration.demo;

import org.geektimes.configuration.microprofile.config.converter.ByteConverter;
import org.geektimes.configuration.microprofile.config.converter.Converters;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/21 20:55
 */
public class TestMicroProfile {

    private Converters converters;

    @Before
    public void init() {
        converters = new Converters();
    }

    @Test
    public void testResolveConvertedType() {
        assertEquals(Byte.class, converters.resolveConvertedType(new ByteConverter()));
    }
}
