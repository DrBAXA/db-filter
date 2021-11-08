package com.t360.filtering.core;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.function.Predicate;

/**
 * @param <T> type of Java representation of the table
 */
public interface QueryNode<T> {

    /**
     * Appends an SQL predicate to provided {@link StringBuilder}
     * without any leading or trailing spaces, so calling code should care about it.
     * See {@link #asSqlWhereClause()}
     *
     * @param queryBuilder a {@link StringBuilder} to append a predicate to.
     */
    default void appendWhereClause(StringBuilder queryBuilder) {
        queryBuilder.append(asSqlWhereClause());
    }

    /**
     * Creates a string predicate that can be used as WHERE clause for prepared statement creation.
     * All required values are provided as '?' placeholders
     * and need to be filled by calling {@link #fillPreparedStatement(PreparedStatement)}
     *
     * @return a String that can be used after WHERE keyword for creating prepared statement
     */
    String asSqlWhereClause();

    void fillPreparedStatement(PreparedStatement preparedStatement);

    // TODO this probably need to be private encapsulated method
    List<PredicateValueDescriptor> collectPredicates();

    /**
     * Creates predicate that can be applied to Java representation of the table row
     *
     * @return predicate that tests objects for corresponding of this Query
     */
    Predicate<T> generateJavaPredicate();

}


