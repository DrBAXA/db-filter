package com.t360.query.filtering;

import com.t360.query.parsing.JsonQuery;

public interface QueryTreeParsingService {

	<T, F extends Enum<F> & ColumnDescription<T>> FilteringNode<T> parse(JsonQuery jsonQuery, Class<F> tableEnum);

}
