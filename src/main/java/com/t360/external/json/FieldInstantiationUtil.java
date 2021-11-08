package com.t360.external.json;

import com.t360.filtering.core.ComparingOperator;
import com.t360.filtering.core.ColumnDescription;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;

/*
 * Not sure if this will be needed and how parsing are done
 */
public class FieldInstantiationUtil {

    public static final String ARRAY_BEGINNING = "[";
    public static final String ARRAY_ENDING = "]";
    public static final String ARRAY_OF_VALUES_SPLIT = "\\s*,\\s*";
    public static final String DOUBLE_QUOTE = "\"";

    public static <F extends ColumnDescription<?>> Object parseValue(F field, ComparingOperator operator, String valueString) {
        valueString = valueString.trim();
        if (valueString.isEmpty()) return null;

        Class<?> fieldType = field.getFieldType();
        if (operator == ComparingOperator.IN || operator == ComparingOperator.NOT_IN) {
            return parseList(valueString, fieldType);
        }
        return parseSingleValue(valueString, fieldType);
    }

    private static Collection<?> parseList(String valueString, Class<?> fieldType) {
        if (valueString.startsWith(ARRAY_BEGINNING)) valueString = valueString.substring(1);
        if (valueString.endsWith(ARRAY_ENDING)) valueString = valueString.substring(0, valueString.length() - 1);

        HashSet<Object> objects = new HashSet<>();
        for (String elementString : valueString.split(ARRAY_OF_VALUES_SPLIT)) {
            Object o = parseSingleValue(elementString, fieldType);
            objects.add(o);
        }
        return objects;

    }

    private static String trimQuotes(String string) {
        if (string.length() >= 2 && string.startsWith(DOUBLE_QUOTE) && string.endsWith(DOUBLE_QUOTE)) {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

    private static Object parseSingleValue(String valueString, Class<?> fieldType) {
        try {
            if (fieldType.equals(String.class)) return trimQuotes(valueString);

            if (fieldType.isPrimitive()) fieldType = getWrapperClass(fieldType);

            // special case for boolean
            if (fieldType.equals(Boolean.class)) return handleBoolean(valueString.trim());

            return fieldType.getConstructor(new Class[]{String.class}).newInstance(valueString);

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to parse string into " + fieldType + ". Class is not supported", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof NumberFormatException) {
                throw processWrongFormatException(valueString, fieldType, (NumberFormatException) e.getCause());
            }
            throw new IllegalStateException("Unable to parse string into " + fieldType + ". Class is not supported", e);
        } catch (NumberFormatException e) {
            throw processWrongFormatException(valueString, fieldType, e);
        }
    }

    private static boolean handleBoolean(String valueString) {
        if (valueString.equalsIgnoreCase("true")) {
            return true;
        } else if (valueString.equalsIgnoreCase("false")) {
            return false;
        } else throw new IllegalArgumentException("Value " + valueString + " can't be parsed as boolean");
    }

    private static IllegalArgumentException processWrongFormatException(String valueString, Class<?> fieldType, NumberFormatException e) {
        return new IllegalArgumentException("Value " + valueString + " can't be parsed as " + fieldType, e);
    }

    public static Class<?> getWrapperClass(Class<?> primitiveClass) {
        if (byte.class.equals(primitiveClass)) {
            return Byte.class;
        } else if (short.class.equals(primitiveClass)) {
            return Short.class;
        } else if (int.class.equals(primitiveClass)) {
            return Integer.class;
        } else if (long.class.equals(primitiveClass)) {
            return Long.class;
        } else if (float.class.equals(primitiveClass)) {
            return Float.class;
        } else if (double.class.equals(primitiveClass)) {
            return Double.class;
        } else if (boolean.class.equals(primitiveClass)) {
            return Boolean.class;
        } else {
            throw new IllegalArgumentException(primitiveClass + " is not a primitive");
        }
    }
}
