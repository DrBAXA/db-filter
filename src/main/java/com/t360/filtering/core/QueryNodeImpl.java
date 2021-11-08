package com.t360.filtering.core;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.function.Predicate;

public class QueryNodeImpl<T> implements QueryNode<T> {


    private final QueryTree<T> root;

    public QueryNodeImpl(QueryTree<T> root) {
        this.root = root;
    }

    @Override
    public String asSqlWhereClause() {
        return root.asSqlWhereClause();
    }

    @Override
    public void fillPreparedStatement(PreparedStatement preparedStatement) {

        // todo move logic here
    }

    @Override
    public List<PredicateValueDescriptor> collectPredicates() {
        return null;
    }

    @Override
    public Predicate<T> generateJavaPredicate() {
        return root.generateJavaPredicate();
    }
}
