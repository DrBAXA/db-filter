package com.t360.filtering.core;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum ComparingOperator {

    EQUAL("=", "="),
    LESS("<", "<"),
    LESS_OR_EQUAL("<=", "<="),
    GREATER(">", ">"),
    GREATER_OR_EQUAL(">=", ">="),
    NOT_EQUAL("<>", "!="),
    IN("IN", "IN"),
    NOT_IN("NOT IN", "NOT IN"),
    IS_NULL("IS NULL", "IS NULL"),
    IS_NOT_NULL("IS NOT NULL", "IS NOT NULL");


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
