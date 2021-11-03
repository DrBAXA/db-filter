package com.t360.filtering.core;

import java.util.function.Predicate;

/**
 * @param <T> type of Java representation of the table
 */
public interface QueryNode<T> {

    /*
     * TODO add a method to fulfill prepared statement
     *  here just set placeholders
     */
    void generateSQLQuery(StringBuilder queryBuilder);

    /**
     * Creates predicate that can be applied to Java representation of the table row
     * @return predicate that tests objects for corresponding of this Query
     */
    Predicate<T> generateJavaPredicate();


}


