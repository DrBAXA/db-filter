package com.t360.query.filtering;

import com.t360.query.filtering.tree.ColumnPredicate;
import com.t360.tables.Negotiation;
import com.t360.external.entities.NegotiationRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.t360.tables.Negotiation.*;
import static org.junit.jupiter.api.Assertions.*;

class ColumnPredicateTest {

	final NegotiationRow negotiationEUR = new NegotiationRow();
	final NegotiationRow negotiationUAH = new NegotiationRow();
	final NegotiationRow negotiationCAD = new NegotiationRow();

	@BeforeEach
	void setUp() {
		negotiationEUR.setCurrency1("EUR");
		negotiationEUR.setSize1(BigDecimal.valueOf(10_000_000));

		negotiationUAH.setCurrency1("UAH");
		negotiationUAH.setSize1(BigDecimal.valueOf(100_000));

		negotiationCAD.setCurrency1("CAD");
		negotiationCAD.setSize1(BigDecimal.valueOf(10_000));
	}

	@Test
	void generateJavaPredicate_EQ() {
		ColumnPredicate<NegotiationRow, Negotiation> predicateCurrency = new ColumnPredicate<>(Currency1, "UAH", ComparingOperator.EQUAL);

		assertFalse(predicateCurrency.generateJavaPredicate().test(negotiationEUR));
		assertTrue(predicateCurrency.generateJavaPredicate().test(negotiationUAH));

		ColumnPredicate<NegotiationRow, Negotiation> predicateSize = new ColumnPredicate<>(Size1, "10000", ComparingOperator.EQUAL);
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationEUR));
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationUAH));
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationCAD));
	}

	@Test
	void generateJavaPredicate_NOT_EQ() {
		ColumnPredicate<NegotiationRow, Negotiation> predicateCurrency = new ColumnPredicate<>(Currency1, "UAH", ComparingOperator.NOT_EQUAL);

		assertTrue(predicateCurrency.generateJavaPredicate().test(negotiationEUR));
		assertFalse(predicateCurrency.generateJavaPredicate().test(negotiationUAH));

		ColumnPredicate<NegotiationRow, Negotiation> predicateSize = new ColumnPredicate<>(Size1, "10000", ComparingOperator.NOT_EQUAL);
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationEUR));
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationUAH));
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationCAD));
	}

	@Test
	void generateJavaPredicate_LESS() {
		ColumnPredicate<NegotiationRow, Negotiation> predicateCurrency = new ColumnPredicate<>(Currency1, "UAH", ComparingOperator.LESS);

		assertTrue(predicateCurrency.generateJavaPredicate().test(negotiationEUR));
		assertFalse(predicateCurrency.generateJavaPredicate().test(negotiationUAH));

		ColumnPredicate<NegotiationRow, Negotiation> predicateSize = new ColumnPredicate<>(Size1, "100000", ComparingOperator.LESS);
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationEUR));
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationUAH));
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationCAD));
	}

	@Test
	void generateJavaPredicate_LESS_OR_EQ() {
		ColumnPredicate<NegotiationRow, Negotiation> predicateCurrency = new ColumnPredicate<>(Currency1, "UAH", ComparingOperator.LESS_OR_EQUAL);

		assertTrue(predicateCurrency.generateJavaPredicate().test(negotiationEUR));
		assertTrue(predicateCurrency.generateJavaPredicate().test(negotiationUAH));

		ColumnPredicate<NegotiationRow, Negotiation> predicateSize = new ColumnPredicate<>(Size1, "100000", ComparingOperator.LESS_OR_EQUAL);
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationEUR));
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationUAH));
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationCAD));
	}

	@Test
	void generateJavaPredicate_GREATER() {
		ColumnPredicate<NegotiationRow, Negotiation> predicateCurrency = new ColumnPredicate<>(Currency1, "UAH", ComparingOperator.GREATER);

		assertFalse(predicateCurrency.generateJavaPredicate().test(negotiationEUR));
		assertFalse(predicateCurrency.generateJavaPredicate().test(negotiationUAH));

		ColumnPredicate<NegotiationRow, Negotiation> predicateSize = new ColumnPredicate<>(Size1, "100000", ComparingOperator.GREATER);
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationEUR));
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationUAH));
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationCAD));
	}

	@Test
	void generateJavaPredicate_GREATER_OR_EQ() {
		ColumnPredicate<NegotiationRow, Negotiation> predicateCurrency = new ColumnPredicate<>(Currency1, "UAH", ComparingOperator.GREATER_OR_EQUAL);

		assertFalse(predicateCurrency.generateJavaPredicate().test(negotiationEUR));
		assertTrue(predicateCurrency.generateJavaPredicate().test(negotiationUAH));

		ColumnPredicate<NegotiationRow, Negotiation> predicateSize = new ColumnPredicate<>(Size1, "100000", ComparingOperator.GREATER_OR_EQUAL);
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationEUR));
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationUAH));
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationCAD));
	}

	@Test
	void generateJavaPredicate_IN() {
		ColumnPredicate<NegotiationRow, Negotiation> predicateCurrency = new ColumnPredicate<>(Currency1, "[UAH, EUR]", ComparingOperator.IN);

		assertTrue(predicateCurrency.generateJavaPredicate().test(negotiationEUR));
		assertTrue(predicateCurrency.generateJavaPredicate().test(negotiationUAH));
		assertFalse(predicateCurrency.generateJavaPredicate().test(negotiationCAD));

		ColumnPredicate<NegotiationRow, Negotiation> predicateSize = new ColumnPredicate<>(Size1, "[10000, 100000]", ComparingOperator.IN);
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationEUR));
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationUAH));
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationCAD));
	}

	@Test
	void generateJavaPredicate_NOT_IN() {
		ColumnPredicate<NegotiationRow, Negotiation> predicateCurrency = new ColumnPredicate<>(Currency1, "[UAH, EUR]", ComparingOperator.NOT_IN);

		assertFalse(predicateCurrency.generateJavaPredicate().test(negotiationEUR));
		assertFalse(predicateCurrency.generateJavaPredicate().test(negotiationUAH));
		assertTrue(predicateCurrency.generateJavaPredicate().test(negotiationCAD));

		ColumnPredicate<NegotiationRow, Negotiation> predicateSize = new ColumnPredicate<>(Size1, "[10000, 100000]", ComparingOperator.NOT_IN);
		assertTrue(predicateSize.generateJavaPredicate().test(negotiationEUR));
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationUAH));
		assertFalse(predicateSize.generateJavaPredicate().test(negotiationCAD));
	}
}