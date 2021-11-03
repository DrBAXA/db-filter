package com.t360.filtering.core;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Value
public class QueryTree<T> implements QueryNode<T> {

    Operator operator;

    List<QueryNode<T>> predicates;

    public QueryTree(Operator operator, List<QueryNode<T>> predicates) {
        this.operator = operator;
        this.predicates = new ArrayList<>(predicates);
    }

    @Override
    public void generateSQLQuery(StringBuilder queryBuilder) {
        for (int i = 0, predicatesSize = predicates.size(); i < predicatesSize; i++) {
            QueryNode<T> predicate = predicates.get(i);
            predicate.generateSQLQuery(queryBuilder);
            if (i < predicatesSize - 1) {
                queryBuilder.append(' ').append(operator).append(' ');
            }
        }
    }

    /*
     * TODO probably need to cache predicates created by subtrees
     */
    @Override
    public Predicate<T> generateJavaPredicate() {
        switch (operator) {
            case AND: return value -> predicates.stream().map(QueryNode::generateJavaPredicate).allMatch(p -> p.test(value));
            case OR: return value -> predicates.stream().map(QueryNode::generateJavaPredicate).anyMatch(p -> p.test(value));
            default: throw new IllegalStateException("Null or new Operator enum value added");
        }
    }

}
