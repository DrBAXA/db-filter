package com.t360.filtering.core;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.function.Predicate;

import static com.fasterxml.jackson.annotation.JsonSubTypes.*;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.*;

@JsonTypeInfo(use = Id.DEDUCTION)
@JsonSubTypes({
        @Type(QueryTree.class),
        @Type(ColumnPredicate.class)
})
public sealed interface QueryNode permits QueryTree, ColumnPredicate {

    /*
     * TODO add a method to fulfill prepared statement
     *  here just set placeholders
     */
    void generateSQLQuery(StringBuilder queryBuilder);

    <T> Predicate<T> generateJavaPredicate();


}


