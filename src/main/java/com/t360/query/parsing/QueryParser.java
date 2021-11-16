package com.t360.query.parsing;

import com.t360.QueryNode;
import com.t360.query.filtering.FilteringNodeImpl;
import com.t360.query.filtering.tree.ColumnPredicate;
import com.t360.query.filtering.tree.QueryTree;
import com.t360.query.filtering.QueryTreeParsingService;
import com.t360.query.filtering.ColumnDescription;
import com.t360.query.filtering.tree.TreeNode;
import com.t360.query.sorting.SortingNodeImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryParser implements QueryTreeParsingService {

	private final ExpressionParser expressionParser = new ExpressionParser();

	@Override
	public <T, F extends Enum<F> & ColumnDescription<T>> QueryNode<T> parse(JsonQuery query, Class<F> tableEnum) {
		String expression = query.getExpression();
		ExpressionParser.Node tree = expressionParser.parse(expression);
		final FilteringNodeImpl<T> filteringNode = new FilteringNodeImpl<>(convertToFilteringNode(tree, query.getPredicates(), tableEnum));

		return new QueryNode<>(filteringNode, getSortingNode(query.getSorting(), tableEnum));
	}

	@NotNull
	private <T, F extends Enum<F> & ColumnDescription<T>> SortingNodeImpl<T, F> getSortingNode(List<SortOrder> orders, Class<F> clazz) {
		List<SortingNodeImpl.Order<T, F>> ordersList = orders == null ? Collections.emptyList() : orders.stream()
				.map(o -> {
					F columnDescriptor = Enum.valueOf(clazz, o.getField());
					return new SortingNodeImpl.Order<>(columnDescriptor, o.getDirection());
				})
				.collect(Collectors.toList());
		return new SortingNodeImpl<>(ordersList);
	}

	private <T, F extends Enum<F> & ColumnDescription<T>> TreeNode<T> convertToFilteringNode(ExpressionParser.Node node,
			Map<String, JsonPredicate> predicates,
			Class<F> tableDescriptor) {
		if (node instanceof ExpressionParser.Tree) {
			final List<TreeNode<T>> subTrees = ((ExpressionParser.Tree) node).getNodes().stream()
					.map(subTree -> convertToFilteringNode(subTree, predicates, tableDescriptor))
					.collect(Collectors.toList());

			return new QueryTree<>(((ExpressionParser.Tree) node).getOperator(), subTrees);
		} else if (node instanceof ExpressionParser.Leaf) {
			String predicateKey = ((ExpressionParser.Leaf) node).getValue();
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

}
