package com.t360.filtering.core.parsing;

import com.t360.filtering.core.QueryNode;
import com.t360.filtering.tables.Negotiation;
import com.t360.filtering.tables.NegotiationRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserTest {

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
                "  \"expression\": \"(A | D) & (B | C)\",\n" +
                "  \"predicates\": {\n" +
                "    \"A\": {\n" +
                "      \"field\": \"Size1\",\n" +
                "      \"value\": 100,\n" +
                "      \"operator\": \">=\"\n" +
                "    },\n" +
                "    \"D\": {\n" +
                "      \"field\": \"Currency1\",\n" +
                "      \"value\": \"CAD\",\n" +
                "      \"operator\": \"=\"\n" +
                "    },\n" +
                "    \"B\": {\n" +
                "      \"field\": \"Size2\",\n" +
                "      \"value\": 10000000,\n" +
                "      \"operator\": \"<\"\n" +
                "    },\n" +
                "    \"C\": {\n" +
                "      \"field\": \"Currency1\",\n" +
                "      \"value\": [\"UAH\", \"EUR\"],\n" +
                "      \"operator\": \"IN\"\n" +
                "    }\n" +
                "  }\n" +
                "}", Negotiation.class);


        assertEquals("((SIZE1 >= ? OR CURRENCY1 = ?) AND (SIZE2 < ? OR CURRENCY1 IN (?, ?)))", queryNode.asSqlWhereClause());

        final NegotiationRow negotiationEUR = new NegotiationRow();
        negotiationEUR.setCurrency1("EUR");
        negotiationEUR.setSize1(BigDecimal.valueOf(10_000_000));
        negotiationEUR.setSize2(BigDecimal.valueOf(10_000_000));

        final NegotiationRow negotiationUAH = new NegotiationRow();
        negotiationUAH.setCurrency1("UAH");
        negotiationUAH.setSize1(BigDecimal.valueOf(10_000));
        negotiationUAH.setSize2(BigDecimal.valueOf(10_000));

        final NegotiationRow negotiationCAD = new NegotiationRow();
        negotiationCAD.setCurrency1("CAD");
        negotiationCAD.setSize1(BigDecimal.valueOf(1_000));
        negotiationCAD.setSize2(BigDecimal.valueOf(1_000_000));

        final NegotiationRow negotiationUSD = new NegotiationRow();
        negotiationUSD.setCurrency1("USD");
        negotiationUSD.setSize1(BigDecimal.valueOf(10));
        negotiationUSD.setSize2(BigDecimal.valueOf(100_000_000));

        assertTrue(queryNode.generateJavaPredicate().test(negotiationUAH));
        assertTrue(queryNode.generateJavaPredicate().test(negotiationEUR));
        assertTrue(queryNode.generateJavaPredicate().test(negotiationCAD));
        assertFalse(queryNode.generateJavaPredicate().test(negotiationUSD));
    }

    @Test
    void parse_wrong() {
        final String missingPredicateQuery = "{\n" +
                "  \"expression\": \"A & (B | C)\",\n" +
                "  \"predicates\": {\n" +
                "    \"A\": {\n" +
                "      \"field\": \"Size1\",\n" +
                "      \"value\": 100,\n" +
                "      \"operator\": \">=\"\n" +
                "    },\n" +
                "    \"B\": {\n" +
                "      \"field\": \"Size2\",\n" +
                "      \"value\": 10000000,\n" +
                "      \"operator\": \"<\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        assertThrows(IllegalArgumentException.class, () -> parsingService.parse(missingPredicateQuery, Negotiation.class));
    }

}