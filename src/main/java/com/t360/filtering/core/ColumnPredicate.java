package com.t360.filtering.core;

import com.t360.filtering.tables.ColumnDescription;
import lombok.Value;

import java.util.function.Predicate;

@Value
public class ColumnPredicate<T, F extends ColumnDescription<T>> implements QueryNode<T> {

    private static final char APOSTROPHE = '\'';
    F field;
    Object value;
    ComparingOperator comparingOperator;

    /*
     * TODO convert to  prepared statement and placeholder
     *  SQL dialects ???
     *  Types probably needed for escaping or switching to prepared statements with placeholders
     */
    @Override
    public void appendWhereClause(StringBuilder queryBuilder) {
        queryBuilder
                .append(field.getColumnName())
                .append(comparingOperator.getSqlSign());
        appendValue(queryBuilder);
    }


    private void appendValue(StringBuilder queryBuilder) {
        if (value instanceof CharSequence) {
            queryBuilder.append('\'').append(value).append('\'');
        } else {
            queryBuilder.append(value);
        }
    }

    @Override
    public Predicate<T> generateJavaPredicate() {
        return createPredicate(field, value, comparingOperator);
    }

    private Predicate<T> createPredicate(F tableEnum, Object value, ComparingOperator operator) {
        return entity -> {
            Object fieldValue = tableEnum.getFieldAccessor().apply(entity);

            // Null conditions first
            if (fieldValue == null) {
                return operator == ComparingOperator.IS_NULL;
            } else if (operator == ComparingOperator.IS_NULL || operator == ComparingOperator.IS_NOT_NULL) {
                return operator == ComparingOperator.IS_NOT_NULL;
            }

            // Null is not permitted for predicate object here
            if (value == null) {
                return false;
//                throw new ParsingException("Null values are not permitted, use IS_NULL or IS_NOT_NULL operators instead"); //
            }

            // Values are of the same class and comparable so just compare
            if ((fieldValue.getClass().isInstance(value))) {
                // we already have checked that value is instance of the class of fieldValue
                // noinspection unchecked,rawtypes
                int c = ((Comparable)fieldValue).compareTo(value);
                return examineComparison(operator, c);
            }

            // If values are both numbers we still can compare
            if (isComparableAsNumbers(fieldValue.getClass(), value.getClass())) {
                return compareNumbers((Number) fieldValue, (Number) value);
            }

            throw new IllegalArgumentException("Wrong data type found");
        };

    }

    private boolean compareNumbers(Number fieldValue, Number value) {
        return false;
    }

    private boolean examineComparison(ComparingOperator operator, int c) {
        switch (operator) {
            case LESS_OR_EQUAL:
                return c <= 0;
            case LESS:
                return c < 0;
            case GREATER:
                return c > 0;
            case GREATER_OR_EQUAL:
                return c >= 0;
            case NOT_EQUAL:
                return c != 0;
            case EQUAL:
                return c == 0;
            default:
                return false;
        }
    }

    private boolean isComparableAsNumbers(Class<?> fieldClass, Class<?> valueClass) {
        return Number.class.isAssignableFrom(fieldClass) && Number.class.isAssignableFrom(valueClass);
    }

}
