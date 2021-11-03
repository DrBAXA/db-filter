package com.t360.filtering.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.function.Predicate;

public final class QueryTree<R> implements QueryNode<R> {

    @JsonProperty
    Operator operator = Operator.AND;

    @JsonProperty
    List<QueryNode<R>> predicates;


    @Override
    public void generateSQLQuery(StringBuilder queryBuilder) {
        for (int i = 0, predicatesSize = predicates.size(); i < predicatesSize; i++) {
            QueryNode<R> predicate = predicates.get(i);
            predicate.generateSQLQuery(queryBuilder);
            if (i < predicatesSize - 1) {
                queryBuilder.append(' ').append(operator).append(' ');
            }
        }
    }

    @Override
    public Predicate<R> generateJavaPredicate() {
        return null;
    }

}
