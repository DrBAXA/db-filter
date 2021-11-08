package com.t360.filtering.core.tree;

import com.t360.filtering.core.LogicalOperator;
import com.t360.filtering.core.PredicateValueDescriptor;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class represents a tree node of an expression tree
 * It can have 0 or more subtrees/leaves all combined by and {@link LogicalOperator}
 *
 * @param <T> Type of entity representing a table row
 */
@Value
public class QueryTree<T> implements TreeNode<T> {

    LogicalOperator operator;
    List<TreeNode<T>> predicates;

    public QueryTree(LogicalOperator operator, List<TreeNode<T>> predicates) {
        this.operator = operator;
        this.predicates = new ArrayList<>(predicates);
    }

    /**
     * Return current node representation in SQL where clause perspective
     *
     * @return {@link String} as sql where clause
     */
    @Override
    public String asSqlWhereClause() {
        return predicates.stream().map(TreeNode::asSqlWhereClause)
                .collect(Collectors.joining(String.format(" %s ", operator.name()), "(", ")"));
    }

    @Override
    public List<PredicateValueDescriptor> collectPredicates() {
        return predicates.stream().map(TreeNode::collectPredicates).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Prepares a {@link Predicate} that can test if an entity satisfies a query
     * that is represented by {@code this} object.
     *
     * @return {@link Predicate} for testing entities
     */
    @Override
    public Predicate<T> generateJavaPredicate() {
        switch (operator) {
            case AND:
                return value -> predicates.stream().map(TreeNode::generateJavaPredicate).allMatch(p -> p.test(value));
            case OR:
                return value -> predicates.stream().map(TreeNode::generateJavaPredicate).anyMatch(p -> p.test(value));
            default:
                throw new IllegalStateException("New Operator enum value added"); // should never happen
        }
    }

}
