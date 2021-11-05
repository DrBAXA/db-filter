package com.t360.filtering.core.parsing;

import com.t360.filtering.core.LogicalOperator;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.t360.filtering.core.LogicalOperator.AND;
import static com.t360.filtering.core.LogicalOperator.OR;

public class ExpressionParser {

    public static final char OR_TOKEN = '|';
    public static final char AND_TOKEN = '&';
    public static final char OPENING_BRACKET = '(';
    public static final char CLOSING_BRACKET = ')';

    public Node parse(String expression) {
        String cleaned = cleanUp(expression);

        return parseOR(cleaned)
                .orElseGet(() -> parseAND(cleaned)
                        .orElseGet(() -> createLeaf(cleaned)));
    }

    public Optional<Node> parseOR(String expression) {
        return splitByOperator(expression, OR_TOKEN, OR);
    }

    public Optional<Node> parseAND(String expression) {
        return splitByOperator(expression, AND_TOKEN, AND);
    }

    private Optional<Node> splitByOperator(String expression, char operatorToken, LogicalOperator operator) {
        final List<String> parts = splitHighLevel(expression, operatorToken);

        // there is no required operators on high level
        if (parts.size() == 1) return Optional.empty();

        final List<Node> nodes = new ArrayList<>();
        for (String part : parts) {
            if (part.isEmpty()) throw new IllegalArgumentException("Invalid expression");
            Node parse = parse(part);
            nodes.add(parse);
        }
        return Optional.of(new Tree(operator, nodes));
    }

    public Node createLeaf(String value) {
        if (!value.matches("\\w+")) {
            throw new IllegalArgumentException("Token '" + value + "' is invalid only alphanumerical or underscore supported");
        }
        return new Leaf(value);
    }

    private String cleanUp(String expression) {
        expression = removeSpaces(expression);
        expression = removeEnclosingBrackets(expression);

        return expression;
    }

    private String removeSpaces(String expression) {
        return expression.replaceAll("\\s+", "");
    }

    private String removeEnclosingBrackets(String expression) {
        while (hasEnclosingBrackets(expression)) {
            expression = expression.substring(1, expression.length() - 1);
        }
        return expression;
    }

    private boolean hasEnclosingBrackets(String expression) {
        return expression.length() >= 2 && expression.charAt(0) == OPENING_BRACKET
                && getBlockEnd(expression, 1) == expression.length();
    }

    private List<String> splitHighLevel(String expression, char operatorToken) {
        List<String> result = new ArrayList<>();

        int start = 0;

        int i = 0;
        while (i < expression.length()) {
            final char currentChar = expression.charAt(i);

            // blocks in brackets are treated as a single token at this level
            if (currentChar == OPENING_BRACKET || currentChar == CLOSING_BRACKET) {
                i = getBlockEnd(expression, i + 1);
                continue;
            } else if (currentChar == operatorToken) {
                result.add(expression.substring(start, i));
                start = i + 1;
            }

            i++;

        }

        // adding last part
        if (start < expression.length()) result.add(expression.substring(start));

        return result;
    }

    /**
     * Searches for a closing bracket that corresponds to a bracket at position {@code i - 1}
     * @param expression full expression string
     * @param i index of a first character after opening bracket. E.g. position to start
     * @return index of a corresponding closing bracket {@code + 1}
     */
    private int getBlockEnd(String expression, int i) {
        int openBracketsCounter = 0;
        while (i < expression.length()) {
            final char c = expression.charAt(i);

            if (c == ExpressionParser.OPENING_BRACKET) {
                openBracketsCounter++;
            } if (c == CLOSING_BRACKET) {
                openBracketsCounter--;
                if (openBracketsCounter < 0) return i + 1;
            }
            i++;
        }
        throw new IllegalArgumentException("Incorrect expression formatting, closing bracket not found");
    }

    interface Node {}

    @Value
    static class Tree implements Node {
        LogicalOperator operator;
        List<Node> nodes;
    }

    @Value
    static class Leaf implements Node {
        String value;
    }
}
