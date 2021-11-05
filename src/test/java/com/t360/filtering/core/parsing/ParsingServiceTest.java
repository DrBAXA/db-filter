package com.t360.filtering.core.parsing;

import com.t360.filtering.core.QueryNode;
import com.t360.filtering.tables.Negotiation;
import com.t360.filtering.tables.NegotiationRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class ParsingServiceTest {

    private QueryParser parsingService;

    @BeforeEach
    void setUp() {
        parsingService = new QueryParser();
    }

    @Test
    void parse_empty() {
        final QueryNode<?> queryNode = parsingService.parse("{\n" +
                "  \"expression\": \"\",\n" +
                "  \"predicates\": {}\n" +
                "}", Negotiation.class);

        final StringBuilder queryBuilder = new StringBuilder();
        queryNode.appendWhereClause(queryBuilder);

        assertEquals("()", queryNode.asSqlWhereClause());
    }

    @Test
    void parse_severalPredicates() {
        final QueryNode<NegotiationRow> queryNode = parsingService.parse("{\n" +
                "  \"expression\": \"A & (B | C)\",\n" +
                "  \"predicates\": {\n" +
                "    \"A\": {\n" +
                "      \"field\": \"Size1\",\n" +
                "      \"value\": 100,\n" +
                "      \"comparingOperator\": \">=\"\n" +
                "    },\n" +
                "    \"B\": {\n" +
                "      \"field\": \"Size2\",\n" +
                "      \"value\": 10000000,\n" +
                "      \"comparingOperator\": \"<\"\n" +
                "    },\n" +
                "    \"C\": {\n" +
                "      \"field\": \"Currency1\",\n" +
                "      \"value\": [\"UAH\", \"EUR\"],\n" +
                "      \"comparingOperator\": \"IN\"\n" +
                "    }\n" +
                "  }\n" +
                "}", Negotiation.class);


        assertEquals("(SIZE1 >= ? AND (SIZE2 < ? OR CURRENCY1 IN (?)))", queryNode.asSqlWhereClause());
    }

}