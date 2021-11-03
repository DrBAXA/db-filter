package com.t360.filtering.core.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.t360.filtering.core.ColumnPredicate;
import com.t360.filtering.core.QueryNode;
import com.t360.filtering.core.QueryTree;
import com.t360.filtering.tables.ColumnDescription;

import java.util.List;
import java.util.stream.Collectors;

public class FlatTreeParsingService implements QueryTreeParsingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /*
     * TODO need to think how to parse general query for different tables
     */
    @Override
    public synchronized <T, F extends Enum<F> & ColumnDescription<T>> QueryNode<T> parse(String jsonQuery, Class<F> tableEnum) throws ParsingException {
        try {
            final JsonQuery query = objectMapper.readValue(jsonQuery, JsonQuery.class);
            return toQueryNode(query, tableEnum);
        } catch (JsonProcessingException e) {
            throw new ParsingException("Unable to parse query tree from the input: " + jsonQuery, e);
        }
    }

    private <T, F extends Enum<F> & ColumnDescription<T>> QueryNode<T> toQueryNode(JsonQuery query, Class<F> tableEnum) {
        if (query instanceof Tree) return convertTree((Tree) query, tableEnum);
        if (query instanceof JsonPredicate) return convertPredicate((JsonPredicate) query, tableEnum);
        throw new IllegalStateException("Unknown subclass " + query.getClass().getName() + " of JsonQuery class!");
    }

    private <T, F extends Enum<F> & ColumnDescription<T>> ColumnPredicate<T, F> convertPredicate(JsonPredicate jsonPredicate, Class<F> tableEnum) {
        final String columnName = jsonPredicate.field;
        F columnDescriptor = Enum.valueOf(tableEnum, columnName);
        return new ColumnPredicate<>(columnDescriptor, jsonPredicate.getValue(), jsonPredicate.getComparingOperator());
    }

    private <T, F extends Enum<F> & ColumnDescription<T>> QueryTree<T> convertTree(Tree tree, Class<F> tableEnum) {
        final List<QueryNode<T>> nodes = tree.getPredicates().stream()
                .map(jsonQuery -> toQueryNode(jsonQuery, tableEnum))
                .collect(Collectors.toList());

        return new QueryTree<>(tree.getOperator(), nodes);
    }

}
