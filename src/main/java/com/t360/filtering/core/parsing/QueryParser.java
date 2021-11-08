package com.t360.filtering.core.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.t360.filtering.core.ColumnPredicate;
import com.t360.filtering.core.QueryNode;
import com.t360.filtering.core.QueryTree;
import com.t360.filtering.tables.ColumnDescription;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.t360.filtering.core.parsing.ExpressionParser.*;

public class QueryParser implements QueryTreeParsingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExpressionParser expressionParser = new ExpressionParser();

    @Override
    public <T, F extends Enum<F> & ColumnDescription<T>> QueryNode<T> parse(String jsonQuery, Class<F> tableEnum) {

        final JsonQuery query = parseJson(jsonQuery);

        String expression = query.getExpression();
        Node tree = expressionParser.parse(expression);

        return convertToQueryNode(tree, query.getPredicates(), tableEnum);
    }

    private <T, F extends Enum<F> & ColumnDescription<T>> QueryNode<T> convertToQueryNode(Node node,
                                                                                          Map<String, JsonPredicate> predicates,
                                                                                          Class<F> tableDescriptor) {
        if (node instanceof Tree) {
            final List<QueryNode<T>> subTrees = ((Tree) node).getNodes().stream()
                    .map(subTree -> convertToQueryNode(subTree, predicates, tableDescriptor))
                    .collect(Collectors.toList());

            return new QueryTree<>(((Tree) node).getOperator(), subTrees);
        } else if (node instanceof Leaf) {
            String predicateKey = ((Leaf) node).getValue();
            JsonPredicate predicate = predicates.get(predicateKey);
            if (predicate == null) {
                throw new IllegalArgumentException("Predicate key '" + predicateKey + "' used in expression is missing");
            }
            return convertPredicate(predicate, tableDescriptor);
        }
        throw new IllegalStateException("Unsupported class"); // should never happen
    }

    private <T, F extends Enum<F> & ColumnDescription<T>> ColumnPredicate<T, F> convertPredicate(JsonPredicate jsonPredicate, Class<F> tableEnum) {
        final String columnName = jsonPredicate.getField();
        F columnDescriptor = Enum.valueOf(tableEnum, columnName);
        return new ColumnPredicate<>(columnDescriptor, jsonPredicate.getValue(), jsonPredicate.getOperator());
    }

    private JsonQuery parseJson(String jsonQuery) {
        try {
            return objectMapper.readValue(jsonQuery, JsonQuery.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }

}
