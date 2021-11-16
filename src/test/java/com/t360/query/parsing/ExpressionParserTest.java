package com.t360.query.parsing;

import com.t360.query.filtering.LogicalOperator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpressionParserTest {

	private ExpressionParser parser = new ExpressionParser();

	@Test
	void parse_empty() {

		final ExpressionParser.Node node = parser.parse("()");
		final ExpressionParser.Node node2 = parser.parse(" ");
		final ExpressionParser.Node node3 = parser.parse("");

		final ExpressionParser.Node expected = new ExpressionParser.Tree(LogicalOperator.OR, Collections.emptyList());

		assertEquals(expected, node);
		assertEquals(expected, node2);
		assertEquals(expected, node3);
	}

	@Test
	void parse_singlePredicate() {

		final ExpressionParser.Node node = parser.parse("A");

		final ExpressionParser.Node expected = new ExpressionParser.Leaf("A");

		assertEquals(expected, node);
	}

	@Test
	void parse_flat_AND() {

		final ExpressionParser.Node tree1 = parser.parse("A & B & C");
		final ExpressionParser.Node tree2 = parser.parse("(A & B & C)");
		final ExpressionParser.Node tree3 = parser.parse("(A& B& C)");

		final ExpressionParser.Tree expected = new ExpressionParser.Tree(LogicalOperator.AND,
				Arrays.asList(new ExpressionParser.Leaf("A"), new ExpressionParser.Leaf("B"), new ExpressionParser.Leaf("C")));

		assertEquals(expected, tree1);
		assertEquals(expected, tree2);
		assertEquals(expected, tree3);
	}

	@Test
	void parse_flat_OR() {

		final ExpressionParser.Node tree1 = parser.parse("A | B | C");
		final ExpressionParser.Node tree2 = parser.parse("(A | B | C)");
		final ExpressionParser.Node tree3 = parser.parse(" A| B| C      ");

		final ExpressionParser.Tree expected = new ExpressionParser.Tree(LogicalOperator.OR,
				Arrays.asList(new ExpressionParser.Leaf("A"), new ExpressionParser.Leaf("B"), new ExpressionParser.Leaf("C")));

		assertEquals(expected, tree1);
		assertEquals(expected, tree2);
		assertEquals(expected, tree3);
	}

	@Test
	void parse_OR_and_AND_withoutBrackets() {

		final ExpressionParser.Node tree1 = parser.parse("A & B | C");
		final ExpressionParser.Tree expected1 = new ExpressionParser.Tree(LogicalOperator.OR,
				Arrays.asList(
						new ExpressionParser.Tree(LogicalOperator.AND, Arrays.asList(new ExpressionParser.Leaf("A"), new ExpressionParser.Leaf("B"))),
						new ExpressionParser.Leaf("C")));
		assertEquals(expected1, tree1);

		final ExpressionParser.Node tree2 = parser.parse("A & B | C & D");
		final ExpressionParser.Tree expected2 = new ExpressionParser.Tree(LogicalOperator.OR,
				Arrays.asList(
						new ExpressionParser.Tree(LogicalOperator.AND, Arrays.asList(new ExpressionParser.Leaf("A"), new ExpressionParser.Leaf("B"))),
						new ExpressionParser.Tree(LogicalOperator.AND, Arrays.asList(new ExpressionParser.Leaf("C"), new ExpressionParser.Leaf("D")))));
		assertEquals(expected2, tree2);

		final ExpressionParser.Node tree3 = parser.parse("A & B | C | D");
		final ExpressionParser.Tree expected3 = new ExpressionParser.Tree(LogicalOperator.OR,
				Arrays.asList(
						new ExpressionParser.Tree(LogicalOperator.AND, Arrays.asList(new ExpressionParser.Leaf("A"), new ExpressionParser.Leaf("B"))),
						new ExpressionParser.Leaf("C"),
						new ExpressionParser.Leaf("D")));
		assertEquals(expected3, tree3);

		final ExpressionParser.Node tree4 = parser.parse("A & B & C | D");
		final ExpressionParser.Tree expected4 = new ExpressionParser.Tree(LogicalOperator.OR,
				Arrays.asList(
						new ExpressionParser.Tree(LogicalOperator.AND,
								Arrays.asList(new ExpressionParser.Leaf("A"), new ExpressionParser.Leaf("B"), new ExpressionParser.Leaf("C"))),
						new ExpressionParser.Leaf("D")));
		assertEquals(expected4, tree4);

	}

	@Test
	void test_parsing_withBrackets() {
		final ExpressionParser.Node tree1 = parser.parse("A & B & (C | D)");
		final ExpressionParser.Tree expected1 = new ExpressionParser.Tree(LogicalOperator.AND,
				Arrays.asList(
						new ExpressionParser.Leaf("A"),
						new ExpressionParser.Leaf("B"),
						new ExpressionParser.Tree(LogicalOperator.OR, Arrays.asList(new ExpressionParser.Leaf("C"), new ExpressionParser.Leaf("D")))));
		assertEquals(expected1, tree1);

		final ExpressionParser.Node tree2 = parser.parse("((A | B) & C) | D");
		final ExpressionParser.Tree expected2 = new ExpressionParser.Tree(LogicalOperator.OR,
				Arrays.asList(
						new ExpressionParser.Tree(LogicalOperator.AND,
								Arrays.asList(
										new ExpressionParser.Tree(LogicalOperator.OR,
												Arrays.asList(new ExpressionParser.Leaf("A"), new ExpressionParser.Leaf("B"))),
										new ExpressionParser.Leaf("C"))),
						new ExpressionParser.Leaf("D")));
		assertEquals(expected2, tree2);

		final ExpressionParser.Node tree3 = parser.parse("((A | B) & (C | D))");
		final ExpressionParser.Tree expected3 = new ExpressionParser.Tree(LogicalOperator.AND,
				Arrays.asList(
						new ExpressionParser.Tree(LogicalOperator.OR, Arrays.asList(new ExpressionParser.Leaf("A"), new ExpressionParser.Leaf("B"))),
						new ExpressionParser.Tree(LogicalOperator.OR, Arrays.asList(new ExpressionParser.Leaf("C"), new ExpressionParser.Leaf("D")))));
		assertEquals(expected3, tree3);

		final ExpressionParser.Node tree4 = parser.parse("A & ( (B | C) | (D | E) )");
		final ExpressionParser.Tree expected4 = new ExpressionParser.Tree(LogicalOperator.AND,
				Arrays.asList(
						new ExpressionParser.Leaf("A"),
						new ExpressionParser.Tree(LogicalOperator.OR,
								Arrays.asList(
										new ExpressionParser.Tree(LogicalOperator.OR,
												Arrays.asList(new ExpressionParser.Leaf("B"), new ExpressionParser.Leaf("C"))),
										new ExpressionParser.Tree(LogicalOperator.OR,
												Arrays.asList(new ExpressionParser.Leaf("D"), new ExpressionParser.Leaf("E")))))));
		assertEquals(expected4, tree4);
	}

	@Test
	void test_parsing_weird() {
		final ExpressionParser.Node tree1 = parser.parse("((((((A & B & ((((C | D))))))))))");
		final ExpressionParser.Tree expected1 = new ExpressionParser.Tree(LogicalOperator.AND,
				Arrays.asList(
						new ExpressionParser.Leaf("A"),
						new ExpressionParser.Leaf("B"),
						new ExpressionParser.Tree(LogicalOperator.OR, Arrays.asList(new ExpressionParser.Leaf("C"), new ExpressionParser.Leaf("D")))));
		assertEquals(expected1, tree1);
	}

	@Test
	void test_parsing_wrong() {
		// missing closing bracket
		assertThrows(IllegalArgumentException.class, () -> parser.parse("(A & B & (C | D)"));
		// missing opening bracket
		assertThrows(IllegalArgumentException.class, () -> parser.parse("A & B & C | D)"));
		// missing predicate on the right side
		assertThrows(IllegalArgumentException.class, () -> parser.parse("A & B & (C | )"));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("A & B & C | "));
		// missing predicate on the left side
		assertThrows(IllegalArgumentException.class, () -> parser.parse("( & B)"));
		assertThrows(IllegalArgumentException.class, () -> parser.parse(" & B & C"));
		// two operators in a row
		assertThrows(IllegalArgumentException.class, () -> parser.parse("A |& B & C"));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("A && B & C"));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("A || B & C"));

		// too long expression
		final String longExpression = "((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((A | ((((((((((((((((((((((((((((((((((((((((B & C))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))";
		assertThrows(IllegalArgumentException.class, () -> parser.parse(longExpression));

	}

}