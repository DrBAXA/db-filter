package com.t360.filtering.core.parsing;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.t360.filtering.core.LogicalOperator.AND;
import static com.t360.filtering.core.LogicalOperator.OR;
import static com.t360.filtering.core.parsing.ExpressionParser.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpressionParserTest {

    private ExpressionParser parser = new ExpressionParser();

    @Test
    void parse_empty() {

        final Node node = parser.parse("()");
        final Node node2 = parser.parse(" ");
        final Node node3 = parser.parse("");

        final Node expected = new Tree(OR, Collections.emptyList());

        assertEquals(expected, node);
        assertEquals(expected, node2);
        assertEquals(expected, node3);
    }

    @Test
    void parse_singlePredicate() {

        final Node node = parser.parse("A");

        final Node expected = new Leaf("A");

        assertEquals(expected, node);
    }

    @Test
    void parse_flat_AND() {

        final Node tree1 = parser.parse("A & B & C");
        final Node tree2 = parser.parse("(A & B & C)");
        final Node tree3 = parser.parse("(A& B& C)");

        final Tree expected = new Tree(AND,
                Arrays.asList(new Leaf("A"), new Leaf("B"), new Leaf("C"))
        );

        assertEquals(expected, tree1);
        assertEquals(expected, tree2);
        assertEquals(expected, tree3);
    }

    @Test
    void parse_flat_OR() {

        final Node tree1 = parser.parse("A | B | C");
        final Node tree2 = parser.parse("(A | B | C)");
        final Node tree3 = parser.parse(" A| B| C      ");

        final Tree expected = new Tree(OR,
                Arrays.asList(new Leaf("A"), new Leaf("B"), new Leaf("C"))
        );

        assertEquals(expected, tree1);
        assertEquals(expected, tree2);
        assertEquals(expected, tree3);
    }

    @Test
    void parse_OR_and_AND_withoutBrackets() {

        final Node tree1 = parser.parse("A & B | C");
        final Tree expected1 = new Tree(OR,
                Arrays.asList(
                        new Tree(AND, Arrays.asList(new Leaf("A"), new Leaf("B"))),
                        new Leaf("C")
                )
        );
        assertEquals(expected1, tree1);

        final Node tree2 = parser.parse("A & B | C & D");
        final Tree expected2 = new Tree(OR,
                Arrays.asList(
                        new Tree(AND, Arrays.asList(new Leaf("A"), new Leaf("B"))),
                        new Tree(AND, Arrays.asList(new Leaf("C"), new Leaf("D")))
                )
        );
        assertEquals(expected2, tree2);

        final Node tree3 = parser.parse("A & B | C | D");
        final Tree expected3 = new Tree(OR,
                Arrays.asList(
                        new Tree(AND, Arrays.asList(new Leaf("A"), new Leaf("B"))),
                        new Leaf("C"),
                        new Leaf("D")
                )
        );
        assertEquals(expected3, tree3);

        final Node tree4 = parser.parse("A & B & C | D");
        final Tree expected4 = new Tree(OR,
                Arrays.asList(
                        new Tree(AND, Arrays.asList(new Leaf("A"), new Leaf("B"), new Leaf("C"))),
                        new Leaf("D")
                )
        );
        assertEquals(expected4, tree4);

    }

    @Test
    void test_parsing_withBrackets() {
        final Node tree1 = parser.parse("A & B & (C | D)");
        final Tree expected1 = new Tree(AND,
                Arrays.asList(
                        new Leaf("A"),
                        new Leaf("B"),
                        new Tree(OR, Arrays.asList(new Leaf("C"), new Leaf("D")))
                )
        );
        assertEquals(expected1, tree1);

        final Node tree2 = parser.parse("((A | B) & C) | D");
        final Tree expected2 = new Tree(OR,
                Arrays.asList(
                        new Tree(AND,
                                Arrays.asList(
                                        new Tree(OR, Arrays.asList(new Leaf("A"), new Leaf("B"))),
                                        new Leaf("C")
                                )
                        ),
                        new Leaf("D")
                )
        );
        assertEquals(expected2, tree2);

        final Node tree3 = parser.parse("((A | B) & (C | D))");
        final Tree expected3 = new Tree(AND,
                Arrays.asList(
                        new Tree(OR, Arrays.asList(new Leaf("A"), new Leaf("B"))),
                        new Tree(OR, Arrays.asList(new Leaf("C"), new Leaf("D")))
                )
        );
        assertEquals(expected3, tree3);

        final Node tree4 = parser.parse("A & ( (B | C) | (D | E) )");
        final Tree expected4 = new Tree(AND,
                Arrays.asList(
                        new Leaf("A"),
                        new Tree(OR,
                                Arrays.asList(
                                        new Tree(OR, Arrays.asList(new Leaf("B"), new Leaf("C"))),
                                        new Tree(OR, Arrays.asList(new Leaf("D"), new Leaf("E")))
                                )
                        )
                )
        );
        assertEquals(expected4, tree4);
    }

    @Test
    void test_parsing_weird() {
        final Node tree1 = parser.parse("((((((A & B & ((((C | D))))))))))");
        final Tree expected1 = new Tree(AND,
                Arrays.asList(
                        new Leaf("A"),
                        new Leaf("B"),
                        new Tree(OR, Arrays.asList(new Leaf("C"), new Leaf("D")))
                )
        );
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
        //two operators in a row
        assertThrows(IllegalArgumentException.class, () -> parser.parse("A |& B & C"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("A && B & C"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("A || B & C"));

        //too long expression
        final String longExpression = "((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((A | ((((((((((((((((((((((((((((((((((((((((B & C))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))";
        assertThrows(IllegalArgumentException.class, () -> parser.parse(longExpression));



    }

}