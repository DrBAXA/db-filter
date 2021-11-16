package com.t360.query.parsing;

import com.t360.external.json.JsonParsingUtil;
import com.t360.query.filtering.FilteringNode;
import com.t360.tables.Negotiation;
import com.t360.external.entities.NegotiationRow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserTest {

	private QueryParser parsingService;

	@BeforeEach
	void setUp() {
		parsingService = new QueryParser();
	}

	@Test
	void parse_empty() {
		final FilteringNode<?> queryNode = parsingService.parse(JsonParsingUtil.parseJson("{\n" +
				"  \"expression\": \"\",\n" +
				"  \"predicates\": {}\n" +
				"}"), Negotiation.class);

		final StringBuilder queryBuilder = new StringBuilder();
		queryNode.appendWhereClause(queryBuilder);

		Assertions.assertEquals("()", queryNode.asSqlWhereClause());
	}

	@Test
	void parse_severalPredicates() {
		final FilteringNode<NegotiationRow> queryNode = parsingService.parse(JsonParsingUtil.parseJson("{\n" +
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
				"}"), Negotiation.class);

		Assertions.assertEquals("((SIZE1 >= ? OR CURRENCY1 = ?) AND (SIZE2 < ? OR CURRENCY1 IN (?, ?)))", queryNode.asSqlWhereClause());

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

		Assertions.assertTrue(queryNode.generateJavaPredicate().test(negotiationUAH));
		Assertions.assertTrue(queryNode.generateJavaPredicate().test(negotiationEUR));
		Assertions.assertTrue(queryNode.generateJavaPredicate().test(negotiationCAD));
		Assertions.assertFalse(queryNode.generateJavaPredicate().test(negotiationUSD));
	}

	@Test
	void parse_wrong() {
		final JsonQuery missingPredicateQuery = JsonParsingUtil.parseJson("{\n" +
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
				"}");

		assertThrows(IllegalArgumentException.class, () -> parsingService.parse(missingPredicateQuery, Negotiation.class));
	}

}