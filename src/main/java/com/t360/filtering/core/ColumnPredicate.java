package com.t360.filtering.core;

import com.fasterxml.jackson.annotation.JsonValue;
import com.t360.filtering.tables.ColumnDescription;
import lombok.Getter;
import lombok.Value;

import java.util.function.Predicate;

@Value
public class ColumnPredicate<T, F extends ColumnDescription<T>> implements QueryNode<T> {

    private static final char APOSTROPHE = '\'';
    F field;
    T value;
    ComparingOperator comparingOperator;

    /*
     * TODO convert to  prepared statement and placeholder
     *  SQL dialects ???
     *  Types probably needed for escaping or switching to prepared statements with placeholders
     */
    @Override
    public void generateSQLQuery(StringBuilder queryBuilder) {
        queryBuilder.append(field).append(comparingOperator.getSqlSign()).append(APOSTROPHE).append(value).append(APOSTROPHE);
    }

    @Override
    public Predicate<T> generateJavaPredicate() {
        return createPredicate(field, value, comparingOperator);
    }

    private Predicate<T> createPredicate(F tableEnum, T value, ComparingOperator operator) {
        return entity -> {
            int c = tableEnum.getFieldAccessor().apply(entity).compareTo(value);
            switch (operator) {
                case LESS_OR_EQUAL: return c <= 0;
                case LESS: return c < 0;
                case GREATER: return c > 0;
                case GREATER_OR_EQUAL: return c >= 0;
                case NOT_EQUAL:  return c != 0;
                case EQUAL:  return c == 0;
                default: return false;
            }
        };
    }


    public enum ComparingOperator {

        EQUAL("=", "="),
        LESS("<", "<"),
        LESS_OR_EQUAL("<=", "<="),
        GREATER(">", ">"),
        GREATER_OR_EQUAL(">=", ">="),
        NOT_EQUAL("!=", "!=")
        ;

        @Getter
        @JsonValue
        private final String jsonSign;
        @Getter
        private final String sqlSign;

        ComparingOperator(String jsonSign, String sqlSign) {
            this.jsonSign = jsonSign;
            this.sqlSign = sqlSign;
        }
    }
}
