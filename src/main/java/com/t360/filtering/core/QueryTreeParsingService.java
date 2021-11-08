package com.t360.filtering.core;

import com.t360.filtering.core.parsing.JsonQuery;

public interface QueryTreeParsingService {

    <T, F extends Enum<F> & ColumnDescription<T>> QueryNode<T> parse(JsonQuery jsonQuery, Class<F> tableEnum);

}
