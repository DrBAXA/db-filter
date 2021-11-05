package com.t360;

import com.t360.database.DatabaseManager;
import com.t360.filtering.core.PredicateValueDescriptor;
import com.t360.filtering.core.QueryNode;
import com.t360.filtering.core.parsing.FlatTreeParsingService;
import com.t360.filtering.core.parsing.ParsingException;
import com.t360.filtering.core.parsing.QueryTreeParsingService;
import com.t360.filtering.tables.Negotiation;
import com.t360.filtering.tables.NegotiationRow;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class App {

    public static void main(String[] args) throws ParsingException, SQLException {
        test1();

        String json = "{\"predicates\": [{\"field\": \"Currency1\", \"value\": \"UAH\", \"comparingOperator\": \"=\"}, {\"field\": \"Size1\", \"value\": 1000000000000000000000.2, \"comparingOperator\": \"<\"}]}";
        test2(json);
    }

    private static void test1() throws ParsingException {
        QueryTreeParsingService parsingService = new FlatTreeParsingService();
        DatabaseManager databaseQueryService = new DatabaseManager();

        TableDataController tableDataController = new TableDataController(parsingService, databaseQueryService);

        final String[][] negotiationTableData = tableDataController.getNegotiationTableData("{\n" +
                "                  \"predicates\": [\n" +
                "                    {\n" +
                "                      \"field\": \"Currency1\",\n" +
                "                      \"value\": \"UAH\",\n" +
                "                      \"comparingOperator\": \"=\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"field\": \"Size1\",\n" +
                "                      \"value\": 1000000000000000000000.2,\n" +
                "                      \"comparingOperator\": \"<\"\n" +
                "                    }\n" +
                "                  ]\n" +
                "                }");

        pring(negotiationTableData);

        final String[][] midMatchStrategyData = tableDataController.getMidMatchStrategyData("{\n" +
                "                  \"predicates\": [\n" +
                "                    {\n" +
                "                      \"field\": \"Symbol\",\n" +
                "                      \"value\": \"TEST\",\n" +
                "                      \"comparingOperator\": \"=\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"field\": \"Spot_sensitivity_price\",\n" +
                "                      \"value\": 1000000000000000000000.2,\n" +
                "                      \"comparingOperator\": \"<\"\n" +
                "                    }\n" +
                "                  ]\n" +
                "                }");

        pring(midMatchStrategyData);
    }

    private static void pring(String[][] negotiationTableData) {
        for (String[] negotiationTableRow : negotiationTableData) {
            for (String column : negotiationTableRow) {
                System.out.print(column + "\t");
            }
            System.out.println();
        }
    }

    private static void test2(String jsonInput) throws ParsingException, SQLException {
        QueryTreeParsingService parsingService = new FlatTreeParsingService();
        QueryNode<NegotiationRow> rootNode = parsingService.parse(jsonInput, Negotiation.class);
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ")
                .append(resolveTableName(Negotiation.class))
                .append(" WHERE ")
                .append(rootNode.asSqlWhereClause());

        System.out.println("\n\nTEST: \n\t" + sqlQuery);

        Consumer<PreparedStatement> applyFunction = ps -> {
            try {
                int index = 1;
                List<PredicateValueDescriptor> descriptors = rootNode.collectPredicates();
                for (PredicateValueDescriptor valHolder : descriptors) {
                    if (valHolder.getFieldType().equals(BigDecimal.class)) {
                        ps.setBigDecimal(index++, (BigDecimal) valHolder.getValue());
                    } else if (valHolder.getFieldType().equals(String.class)) {
                        ps.setString(index++, (String) valHolder.getValue());
                    } else if (valHolder.getFieldType().equals(Boolean.class)) {
                        ps.setBoolean(index++, (Boolean) valHolder.getValue());
                    }
//                    else if () {
//                        Array array = statement.getConnection().createArrayOf("VARCHAR", new Object[]{"A1", "B2","C3"});
//                        statement.setArray(1, array);
//                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalStateException("Cant correctly configure prepare statement: " + e.getSQLState());
            }
        };
        List<String> result = DatabaseManager.execute(sqlQuery.toString(), applyFunction);
        result.forEach(System.out::println);
    }

    private static <T extends Enum<T>> String resolveTableName(Class<T> table) {
        return table.getSimpleName().toUpperCase();
    }
}