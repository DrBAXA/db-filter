package com.t360.filtering.tables;

import java.util.function.Function;

/**
 *
 * @param <T> type of java representation of the database table row
 */
public interface ColumnDescription<T> {

    String getColumnName();

    Class<?> getFieldType();

    Function<T, ?> getFieldAccessor();

}
