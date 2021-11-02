package com.t360.filtering.core.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.t360.filtering.core.QueryNode;

public class FlatTreeParsingService implements QueryTreeParsingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public QueryNode parse(String jsonQuery) throws ParsingException {
        try {
            return objectMapper.readValue(jsonQuery, QueryNode.class);
        } catch (JsonProcessingException e) {
            throw new ParsingException("Unable to parse query tree from the input: " + jsonQuery, e);
        }
    }

}
