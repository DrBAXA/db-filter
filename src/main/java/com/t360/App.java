package com.t360;

import com.t360.database.DatabaseManager;
import com.t360.filtering.core.PredicateValueDescriptor;
import com.t360.filtering.core.QueryNode;
import com.t360.filtering.core.parsing.FlatTreeParsingService;
import com.t360.filtering.core.parsing.ParsingException;
import com.t360.filtering.core.parsing.QueryTreeParsingService;
import com.t360.filtering.tables.ColumnDescription;
import com.t360.filtering.tables.MidMatchStrategy;
import com.t360.filtering.tables.Negotiation;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class App {

    public static void main(String[] args) throws ParsingException, SQLException {
        String json = "{\"predicates\": [{\"field\": \"Currency1\", \"value\": \"UAH\", \"comparingOperator\": \"=\"}, {\"field\": \"Size1\", \"value\": 1000000000000000000000.2, \"comparingOperator\": \"<\"}]}";
        executeJsonQuery(json, Negotiation.class);

        String json2 = "{\"predicates\": [{\"field\": \"Symbol\", \"value\": \"AA\", \"comparingOperator\": \"=\"}]}";
        executeJsonQuery(json2, MidMatchStrategy.class);
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

        print(negotiationTableData);

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

        print(midMatchStrategyData);
    }

    private static void print(String[][] negotiationTableData) {
        for (String[] negotiationTableRow : negotiationTableData) {
            for (String column : negotiationTableRow) {
                System.out.print(column + "\t");
            }
            System.out.println();
        }
    }

    private static <T, F extends Enum<F> & ColumnDescription<T>> void executeJsonQuery(String jsonInput, Class<F> tableEnum) throws ParsingException, SQLException {
        QueryTreeParsingService parsingService = new FlatTreeParsingService();
        QueryNode<T> rootNode = parsingService.parse(jsonInput, tableEnum);
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ")
                .append(resolveTableName(tableEnum))
                .append(" WHERE ")
                .append(rootNode.asSqlWhereClause());

        System.out.println("\nTEST: \n\t" + sqlQuery + "\n");

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
                    } else if (valHolder.getValue() instanceof Collection) {
                        Object[] array = ((Collection<?>) valHolder.getValue()).toArray();
                        String typeName = resolveJdbcType(array[0]);
                        ps.setArray(index++, ps.getConnection().createArrayOf(typeName, array));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalStateException("Cant correctly configure prepare statement: " + e.getSQLState());
            }
        };
        List<String> result = DatabaseManager.execute(sqlQuery.toString(), applyFunction);
        result.forEach(System.out::println);
    }

    private static String resolveJdbcType(Object obj) {
        if (obj instanceof String) return JDBCType.VARCHAR.getName();
        else if (obj instanceof Long) return JDBCType.BIGINT.getName();
        else if (obj instanceof Integer) return JDBCType.INTEGER.getName();
        else if (obj instanceof BigDecimal) return JDBCType.DECIMAL.getName();
        else throw new IllegalStateException("Does not support '" + obj.getClass().getSimpleName() + "' type yet");
    }

    private static <T extends Enum<T>> String resolveTableName(Class<T> table) {
        //todo use camelCase to _ transformation if its possible
        if (table.equals(Negotiation.class)) return "NEGOTIATION";
        if (table.equals(MidMatchStrategy.class)) return "MID_MATCH_STRATEGY";
        throw new IllegalStateException(table.getSimpleName() + " table not supported yet");
    }
}