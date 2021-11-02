package com.t360.filtering.core.parsing;

import com.t360.filtering.core.QueryNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlatTreeParsingServiceTest {

    private FlatTreeParsingService parsingService;

    @BeforeEach
    void setUp() {
        parsingService = new FlatTreeParsingService();
    }

    @Test
    void parse_empty_() {
        assertThrows(ParsingException.class, () -> parsingService.parse("{}"));
    }

    @Test
    void parse_empty() throws ParsingException {
        final QueryNode queryNode = parsingService.parse("{\"predicates\": []}");

        final StringBuilder queryBuilder = new StringBuilder();
        queryNode.generateSQLQuery(queryBuilder);

        assertTrue(queryBuilder.isEmpty());
    }

    @Test
    void parse_onePredicate() throws ParsingException {
        final QueryNode queryNode = parsingService.parse("""
                {
                  "predicates": [
                    {
                      "field": "Currency1",
                      "value": "UAH",
                      "comparingOperator": "!="
                    }
                  ]
                }
                """);

        final StringBuilder queryBuilder = new StringBuilder();
        queryNode.generateSQLQuery(queryBuilder);

        assertEquals("Currency1!='UAH'", queryBuilder.toString());
    }

    @Test
    void parse_severalPredicates() throws ParsingException {
        final QueryNode queryNode = parsingService.parse("""
                {
                  "predicates": [
                    {
                      "field": "Currency1",
                      "value": "UAH",
                      "comparingOperator": "!="
                    },{
                      "field": "Product",
                      "value": "TEST",
                      "comparingOperator": "="
                    }
                  ]
                }
                """);

        final StringBuilder queryBuilder = new StringBuilder();
        queryNode.generateSQLQuery(queryBuilder);

        assertEquals("Currency1!='UAH' AND Product='TEST'", queryBuilder.toString());
    }
}