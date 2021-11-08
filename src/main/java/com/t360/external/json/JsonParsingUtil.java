package com.t360.external.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.t360.filtering.core.parsing.JsonQuery;

public class JsonParsingUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonQuery parseJson(String jsonQuery) {
        try {
            return objectMapper.readValue(jsonQuery, JsonQuery.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }
}
