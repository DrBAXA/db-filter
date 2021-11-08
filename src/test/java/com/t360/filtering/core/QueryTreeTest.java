package com.t360.filtering.core;

import com.t360.external.entities.NegotiationRow;
import com.t360.filtering.core.tree.QueryTree;
import com.t360.filtering.core.tree.TreeNode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryTreeTest {

    private final TreeNode<NegotiationRow> currencyUAH = new TreeNode<NegotiationRow>() {
        @Override
        public Predicate<NegotiationRow> generateJavaPredicate() {
            return v -> v != null && v.getCurrency1() != null && v.getCurrency1().equals("UAH");
        }

        @Override
        public String asSqlWhereClause() {
            return null;
        }

        @Override
        public List<PredicateValueDescriptor> collectPredicates() {
            return null;
        }
    };

    private final TreeNode<NegotiationRow> currencyEUR = new TreeNode<NegotiationRow>() {
        @Override
        public Predicate<NegotiationRow> generateJavaPredicate() {
            return v -> v != null && v.getCurrency1() != null && v.getCurrency1().equals("EUR");
        }

        @Override
        public String asSqlWhereClause() {
            return null;
        }

        @Override
        public List<PredicateValueDescriptor> collectPredicates() {
            return null;
        }
    };

    private final TreeNode<NegotiationRow> sizeLessThanMillion = new TreeNode<NegotiationRow>() {
        @Override
        public Predicate<NegotiationRow> generateJavaPredicate() {
            return v -> v != null && v.getSize1() != null && v.getSize1().compareTo(BigDecimal.valueOf(1000000L)) < 0;
        }

        @Override
        public String asSqlWhereClause() {
            return null;
        }

        @Override
        public List<PredicateValueDescriptor> collectPredicates() {
            return null;
        }
    };

    @Test
    void emptyTree() {
        final QueryTree<NegotiationRow> queryTree = new QueryTree<>(LogicalOperator.AND, Collections.emptyList());

        final NegotiationRow negotiationEUR = new NegotiationRow();
        negotiationEUR.setCurrency1("EUR");
        negotiationEUR.setSize1(BigDecimal.valueOf(10_000_000));

        final NegotiationRow negotiationUAH = new NegotiationRow();
        negotiationUAH.setCurrency1("UAH");
        negotiationUAH.setSize1(BigDecimal.valueOf(10_000));

        assertTrue(queryTree.generateJavaPredicate().test(negotiationUAH));
        assertTrue(queryTree.generateJavaPredicate().test(negotiationEUR));
    }

    @Test
    void flat_AND() {
        final QueryTree<NegotiationRow> queryTree = new QueryTree<>(LogicalOperator.AND, Arrays.asList(
                currencyUAH,
                sizeLessThanMillion
        ));

        final NegotiationRow negotiationEUR = new NegotiationRow();
        negotiationEUR.setCurrency1("EUR");
        negotiationEUR.setSize1(BigDecimal.valueOf(10_000_000));

        final NegotiationRow negotiationUAH = new NegotiationRow();
        negotiationUAH.setCurrency1("UAH");
        negotiationUAH.setSize1(BigDecimal.valueOf(10_000));

        assertTrue(queryTree.generateJavaPredicate().test(negotiationUAH));
        assertFalse(queryTree.generateJavaPredicate().test(negotiationEUR));
    }

    @Test
    void flat_OR() {

        final QueryTree<NegotiationRow> queryTree = new QueryTree<>(LogicalOperator.OR, Arrays.asList(
                currencyUAH,
                sizeLessThanMillion
        ));

        final NegotiationRow negotiationEUR = new NegotiationRow();
        negotiationEUR.setCurrency1("EUR");
        negotiationEUR.setSize1(BigDecimal.valueOf(10_000_000));

        final NegotiationRow negotiationUAH = new NegotiationRow();
        negotiationUAH.setCurrency1("UAH");
        negotiationUAH.setSize1(BigDecimal.valueOf(10_000));

        final NegotiationRow negotiationCAD = new NegotiationRow();
        negotiationCAD.setCurrency1("CAD");
        negotiationCAD.setSize1(BigDecimal.valueOf(10_000));

        assertTrue(queryTree.generateJavaPredicate().test(negotiationUAH));
        assertFalse(queryTree.generateJavaPredicate().test(negotiationEUR));
        assertTrue(queryTree.generateJavaPredicate().test(negotiationCAD));
    }

    @Test
    void tree() {

        final QueryTree<NegotiationRow> queryTree = new QueryTree<>(LogicalOperator.OR, Arrays.asList(
                new QueryTree<>(LogicalOperator.AND, Arrays.asList(
                        currencyUAH,
                        sizeLessThanMillion)
                ),
                currencyEUR
        ));

        final NegotiationRow negotiationEUR = new NegotiationRow();
        negotiationEUR.setCurrency1("EUR");
        negotiationEUR.setSize1(BigDecimal.valueOf(10_000_000));

        final NegotiationRow negotiationUAH = new NegotiationRow();
        negotiationUAH.setCurrency1("UAH");
        negotiationUAH.setSize1(BigDecimal.valueOf(10_000));

        final NegotiationRow negotiationCAD = new NegotiationRow();
        negotiationCAD.setCurrency1("CAD");
        negotiationCAD.setSize1(BigDecimal.valueOf(10_000));

        assertTrue(queryTree.generateJavaPredicate().test(negotiationUAH));
        assertTrue(queryTree.generateJavaPredicate().test(negotiationEUR));
        assertFalse(queryTree.generateJavaPredicate().test(negotiationCAD));
    }

    @Test
    void nullHandling() {
        final QueryTree<NegotiationRow> queryTree = new QueryTree<>(LogicalOperator.AND, Arrays.asList(
                new QueryTree<>(LogicalOperator.OR, Arrays.asList(
                        currencyUAH,
                        sizeLessThanMillion)
                ),
                currencyEUR
        ));

        final NegotiationRow negotiationEUR = new NegotiationRow();
        negotiationEUR.setCurrency1("EUR");
        negotiationEUR.setSize1(null);

        assertFalse(queryTree.generateJavaPredicate().test(negotiationEUR));
    }

}