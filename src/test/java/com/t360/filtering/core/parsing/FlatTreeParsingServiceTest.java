package com.t360.filtering.core.parsing;

import com.t360.filtering.core.QueryNode;
import com.t360.filtering.tables.Negotiation;
import com.t360.filtering.tables.NegotiationRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class FlatTreeParsingServiceTest {

    private FlatTreeParsingService parsingService;

    @BeforeEach
    void setUp() {
        parsingService = new FlatTreeParsingService();
    }

    @Test
    void parse_empty_() {
        assertThrows(ParsingException.class, () -> parsingService.parse("{}", Negotiation.class));
    }

    @Test
    void parse_empty() throws ParsingException {
        final QueryNode<?> queryNode = parsingService.parse("{\"predicates\": []}", Negotiation.class);

        final StringBuilder queryBuilder = new StringBuilder();
        queryNode.generateSQLQuery(queryBuilder);

        assertEquals(0, queryBuilder.length());
    }

    @Test
    void parse_onePredicate() throws ParsingException {
        final QueryNode<NegotiationRow> queryNode = parsingService.parse("{\n" +
                "                  \"predicates\": [\n" +
                "                    {\n" +
                "                      \"field\": \"Currency1\",\n" +
                "                      \"value\": \"UAH\",\n" +
                "                      \"comparingOperator\": \"!=\"\n" +
                "                    }\n" +
                "                  ]\n" +
                "                }", Negotiation.class);

        final StringBuilder queryBuilder = new StringBuilder();
        queryNode.generateSQLQuery(queryBuilder);

        assertEquals("Currency1!='UAH'", queryBuilder.toString());
    }

    @Test
    void parse_severalPredicates() throws ParsingException {
        final QueryNode<NegotiationRow> queryNode = parsingService.parse("{\n" +
                "                  \"predicates\": [\n" +
                "                    {\n" +
                "                      \"field\": \"Currency1\",\n" +
                "                      \"value\": \"UAH\",\n" +
                "                      \"comparingOperator\": \"!=\"\n" +
                "                    },{\n" +
                "                      \"field\": \"Product\",\n" +
                "                      \"value\": \"TEST\",\n" +
                "                      \"comparingOperator\": \"=\"\n" +
                "                    }\n" +
                "                  ]\n" +
                "                }", Negotiation.class);

        final StringBuilder queryBuilder = new StringBuilder();
        queryNode.generateSQLQuery(queryBuilder);

        assertEquals("Currency1!='UAH' AND Product='TEST'", queryBuilder.toString());
    }

    @Test
    void testPredicate_singular() throws ParsingException {
        final QueryNode<NegotiationRow> queryNode = parsingService.parse("{\n" +
                "                  \"predicates\": [\n" +
                "                    {\n" +
                "                      \"field\": \"Currency1\",\n" +
                "                      \"value\": \"UAH\",\n" +
                "                      \"comparingOperator\": \"!=\"\n" +
                "                    }\n" +
                "                  ]\n" +
                "                }", Negotiation.class);
        final Predicate<NegotiationRow> negotiationRowPredicate = queryNode.generateJavaPredicate();

        final NegotiationRow negotiationEUR = new NegotiationRow();
        negotiationEUR.setCurrency1("EUR");

        final NegotiationRow negotiationUAH = new NegotiationRow();
        negotiationUAH.setCurrency1("UAH");

        assertTrue(negotiationRowPredicate.test(negotiationEUR));
        assertFalse(negotiationRowPredicate.test(negotiationUAH));
    }
}