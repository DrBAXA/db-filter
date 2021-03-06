package com.t360.query.filtering;

import com.t360.query.filtering.tree.TreeNode;

import java.sql.PreparedStatement;

/**
 * @param <T>
 *            type of Java representation of the table
 */
public interface FilteringNode<T> extends TreeNode<T> {

	/**
	 * Appends an SQL predicate to provided {@link StringBuilder}
	 * without any leading or trailing spaces, so calling code should care about it.
	 * See {@link #asSqlWhereClause()}
	 *
	 * @param queryBuilder
	 *            a {@link StringBuilder} to append a predicate to.
	 */
	default void appendWhereClause(StringBuilder queryBuilder) {
		queryBuilder.append(asSqlWhereClause());
	}

	void fillPreparedStatement(PreparedStatement preparedStatement);

}
