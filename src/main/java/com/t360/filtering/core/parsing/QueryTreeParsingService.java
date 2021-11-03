package com.t360.filtering.core.parsing;

import com.t360.filtering.core.QueryNode;
import com.t360.filtering.tables.ColumnDescription;

public interface QueryTreeParsingService {

    <T, F extends Enum<F> & ColumnDescription<T>> QueryNode<T> parse(String jsonQuery, Class<F> tableEnum) throws ParsingException;

}
