package com.t360.filtering;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.t360.filtering.core.QueryNode;
import org.junit.jupiter.api.Test;

class QueryNodeDeserializeTest {

    @Test
    void testDeserialize() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = "{\n" +
                "  \"predicates\": [\n" +
                "    {\n" +
                "      \"field\": \"name\",\n" +
                "      \"value\": \"SomeName\",\n" +
                "      \"comparingOperator\": \"=\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        QueryNode jsonQuery = objectMapper.readValue(content, QueryNode.class);

        System.out.println(jsonQuery);

    }
}