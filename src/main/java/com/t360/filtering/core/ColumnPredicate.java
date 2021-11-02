package com.t360.filtering.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.Getter;

import java.util.function.Predicate;

@Data
public final class ColumnPredicate<T extends Enum<T>> implements QueryNode {

    @JsonProperty
    String field;
    @JsonProperty
    Object value;
    @JsonProperty
    ComparingOperator comparingOperator;

    /*
     * TODO convert to  prepared statement and placeholder
     *  SQL dialects ???
     *  Types probably needed for escaping or switching to prepared statements with placeholders
     */
    @Override
    public void generateSQLQuery(StringBuilder queryBuilder) {
        queryBuilder.append(field).append(comparingOperator.getSqlSign()).append('\'').append(value).append('\'');
    }

    @Override
    public <R> Predicate<R> generateJavaPredicate() {
        return null;
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
