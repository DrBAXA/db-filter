package com.t360.filtering.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.t360.filtering.tables.ColumnDescription;
import lombok.Data;
import lombok.Getter;

import java.util.function.Predicate;

@Data
public final class ColumnPredicate<R, T extends Enum<T> & ColumnDescription<R>> implements QueryNode<R> {

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
    public Predicate<R> generateJavaPredicate() {
        return createPredicate(null, value, comparingOperator);
    }

    /*
     * TODO Illia pleas implement this method
     *  When I will finish my work we will use fields here but for now use these arguments please
     *  Currently field is just a string but I'm working to convert it into an enum value
     */
    private Predicate<R> createPredicate(T tableEnum, Object value, ComparingOperator operator) {
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
