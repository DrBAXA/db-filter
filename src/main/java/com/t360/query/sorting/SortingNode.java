package com.t360.query.sorting;

import java.util.Comparator;

public interface SortingNode<T> {

	/**
	 * Appends an SQL predicate to provided {@link StringBuilder}
	 * without any leading or trailing spaces, so calling code should care about it.
	 * See {@link #asSqlOrderByClause()}
	 *
	 * @param queryBuilder
	 *            a {@link StringBuilder} to append a predicate to.
	 */
	default void appendWhereClause(StringBuilder queryBuilder) {
		queryBuilder.append(asSqlOrderByClause());
	}

	/**
	 * Creates a string predicate that can be used after ORDER BY clause for prepared statement creation.
	 * Returns empty string if there is no ordering, so calling code should check it
	 * to not send query with just ORDER BY without any columns as it's not valid SQL query
	 *
	 * @return a String that can be used after ORDER BY keyword for creating prepared statement
	 */
	String asSqlOrderByClause();

	Comparator<T> generateComparator();
}
