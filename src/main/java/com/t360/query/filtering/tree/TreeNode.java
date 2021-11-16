package com.t360.query.filtering.tree;

import com.t360.query.filtering.PredicateValueDescriptor;

import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a query tree node either a leaf or tree
 *
 * @param <T>
 *            type of Java representation of the table
 */
public interface TreeNode<T> {

	/**
	 * Creates a string predicate that can be used after WHERE clause for prepared statement creation.
	 * All required values are provided as '?' placeholders.
	 * Returns empty string if there is no filters, so calling code should check it
	 * to not send query with just WHERE without any predicates as it's not valid SQL query
	 *
	 * @return a String that can be used after WHERE keyword for creating prepared statement
	 */
	String asSqlWhereClause();

	List<PredicateValueDescriptor> collectPredicates();

	/**
	 * Creates predicate that can be applied to Java representation of the table row
	 *
	 * @return predicate that tests objects for corresponding of this Query
	 */
	Predicate<T> generateJavaPredicate();

}
