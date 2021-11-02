package com.t360.filtering.core.parsing;

import com.t360.filtering.core.QueryNode;

public interface QueryTreeParsingService {

    QueryNode parse(String jsonQuery) throws ParsingException;

}
