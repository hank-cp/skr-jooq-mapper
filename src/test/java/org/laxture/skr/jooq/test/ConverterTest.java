package org.laxture.skr.jooq.test;

import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.converter.number.Double2BigDecimalConverter;
import org.laxture.skr.jooq.mapper.converter.number.Long2BigIntegerConverter;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    @Test
    void testStringConverter() {
        StringConverter converter = new StringConverter();

        assertTrue(converter.match("test", "test") >= 0);
        assertEquals("hello", converter.convertToModelType("hello"));
        assertEquals("world", converter.convertToJooqType("world"));
    }

    @Test
    void testIntegerConverter() {
        IntegerConverter converter = new IntegerConverter();

        assertTrue(converter.match(42, 42) >= 0);
        assertEquals(42, converter.convertToModelType(42));
        assertEquals(42, converter.convertToJooqType(42));
    }

    @Test
    void testLongConverter() {
        Long2BigIntegerConverter converter = new Long2BigIntegerConverter();

        assertTrue(converter.match(100L, 100L) >= 0);
        assertEquals(100L, converter.convertToModelType(100L));
        assertEquals(100L, converter.convertToJooqType(100L));
    }

    @Test
    void testBooleanConverter() {
        BooleanConverter converter = new BooleanConverter();

        assertTrue(converter.match(true, true) >= 0);
        assertEquals(true, converter.convertToModelType(true));
        assertEquals(false, converter.convertToModelType(false));
        assertEquals(true, converter.convertToModelType(1));
        assertEquals(false, converter.convertToModelType(0));
    }

    @Test
    void testDoubleConverter() {
        Double2BigDecimalConverter converter = new Double2BigDecimalConverter();

        assertTrue(converter.match(3.14, 3.14) >= 0);
        assertEquals(3.14, converter.convertToModelType(3.14));
        assertEquals(3.14, converter.convertToJooqType(3.14));
    }

    @Test
    void testFloatConverter() {
        FloatConverter converter = new FloatConverter();

        assertTrue(converter.match(2.5f, 2.5f) >= 0);
        assertEquals(2.5f, converter.convertToModelType(2.5f));
        assertEquals(2.5f, converter.convertToJooqType(2.5f));
    }
}
