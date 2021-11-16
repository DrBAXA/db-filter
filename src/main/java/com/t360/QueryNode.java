package com.t360;

import com.t360.query.filtering.FilteringNode;
import com.t360.query.sorting.SortingNode;
import lombok.Value;
import lombok.experimental.Delegate;

@Value
public class QueryNode<T> implements SortingNode<T>, FilteringNode<T> {

	@Delegate
	FilteringNode<T> filteringNode;

	@Delegate
	SortingNode<T> sortingNode;
}
