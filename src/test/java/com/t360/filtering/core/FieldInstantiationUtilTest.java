package com.t360.filtering.core;

import com.t360.filtering.tables.ColumnDescription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;

import static com.t360.filtering.core.ComparingOperator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class FieldInstantiationUtilTest {

    @Mock
    ColumnDescription<?> columnDescription;

    @Test
    void parseValue_primitive() {
        doReturn(int.class).when(columnDescription).getFieldType();
        assertEquals(123, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(long.class).when(columnDescription).getFieldType();
        assertEquals(123L, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(short.class).when(columnDescription).getFieldType();
        assertEquals((short) 123, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(byte.class).when(columnDescription).getFieldType();
        assertEquals((byte) 123, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(float.class).when(columnDescription).getFieldType();
        assertEquals(123f, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(double.class).when(columnDescription).getFieldType();
        assertEquals(123d, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(boolean.class).when(columnDescription).getFieldType();
        assertEquals(true, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "true"));
    }

    @Test
    void parseValue_primitive_wrongValue() {
        doReturn(int.class).when(columnDescription).getFieldType();
        assertThrows(IllegalArgumentException.class, () -> FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123.5"));

        doReturn(long.class).when(columnDescription).getFieldType();
        assertThrows(IllegalArgumentException.class, () -> FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123.5"));

        doReturn(short.class).when(columnDescription).getFieldType();
        assertThrows(IllegalArgumentException.class, () -> FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123.5"));

        doReturn(byte.class).when(columnDescription).getFieldType();
        assertThrows(IllegalArgumentException.class, () -> FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123.5"));

        doReturn(float.class).when(columnDescription).getFieldType();
        assertThrows(IllegalArgumentException.class, () -> FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123someText"));

        doReturn(double.class).when(columnDescription).getFieldType();
        assertThrows(IllegalArgumentException.class, () -> FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123someText"));

        doReturn(boolean.class).when(columnDescription).getFieldType();
        assertThrows(IllegalArgumentException.class, () -> FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123someText"));
    }

    @Test
    void parseValue_wrappers() {
        doReturn(Integer.class).when(columnDescription).getFieldType();
        assertEquals(123, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(Long.class).when(columnDescription).getFieldType();
        assertEquals(123L, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(Short.class).when(columnDescription).getFieldType();
        assertEquals((short) 123, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(Byte.class).when(columnDescription).getFieldType();
        assertEquals((byte) 123, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(Float.class).when(columnDescription).getFieldType();
        assertEquals(123f, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(Double.class).when(columnDescription).getFieldType();
        assertEquals(123d, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "123"));

        doReturn(Boolean.class).when(columnDescription).getFieldType();
        assertEquals(true, FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "true"));
    }

    @Test
    void parseValue_bigNumbers() {
        doReturn(BigInteger.class).when(columnDescription).getFieldType();
        assertEquals(new BigInteger("12345678942345646879894653216546845"), FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "12345678942345646879894653216546845"));

        doReturn(BigDecimal.class).when(columnDescription).getFieldType();
        assertEquals(new BigDecimal("12345678942345646879894653216546845.123456"), FieldInstantiationUtil.parseValue(columnDescription, EQUAL, "12345678942345646879894653216546845.123456"));
    }

    @Test
    void parseValue_empty() {
        assertNull(FieldInstantiationUtil.parseValue(columnDescription, EQUAL, ""));
    }

    @Test
    void parseValue_array() {
        doReturn(Integer.class).when(columnDescription).getFieldType();
        assertEquals(new HashSet<>(Arrays.asList(10, 20 ,30)), FieldInstantiationUtil.parseValue(columnDescription, IN, "[10, 20, 30]"));
        assertEquals(new HashSet<>(Arrays.asList(10, 20 ,30)), FieldInstantiationUtil.parseValue(columnDescription, IN, "10, 20, 30"));
    }
}