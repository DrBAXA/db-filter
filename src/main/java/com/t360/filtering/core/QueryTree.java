package com.t360.filtering.core;

import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Class represents a tree node of an expression tree
 * It can have 0  or more subtrees/leaves all combined by an {@link Operator}
 * @param <T> Type of entity representing a table row
 */
@Value
public class QueryTree<T> implements QueryNode<T> {

    Operator operator;

    List<QueryNode<T>> predicates;

    public QueryTree(@NonNull Operator operator, @NonNull List<QueryNode<T>> predicates) {
        this.operator = operator;
        this.predicates = new ArrayList<>(predicates);
    }

    /**
     * Appends an SQL predicate to provided {@link StringBuilder}
     * without any leading or trailing spaces, so calling code should care about it.
     *
     * @param queryBuilder a {@link StringBuilder} to append a predicate to.
     */
    @Override
    public void appendWhereClause(StringBuilder queryBuilder) {
        if (predicates.isEmpty()) return;

        queryBuilder.append("(");

        for (int i = 0, predicatesSize = predicates.size(); i < predicatesSize; i++) {
            QueryNode<T> predicate = predicates.get(i);
            predicate.appendWhereClause(queryBuilder);
            if (i < predicatesSize - 1) {
                queryBuilder.append(' ').append(operator).append(' ');
            }
        }

        queryBuilder.append(")");
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
            case AND: return value -> predicates.stream().map(QueryNode::generateJavaPredicate).allMatch(p -> p.test(value));
            case OR: return value -> predicates.stream().map(QueryNode::generateJavaPredicate).anyMatch(p -> p.test(value));
            default: throw new IllegalStateException("New Operator enum value added"); // should never happen
        }
    }

}
