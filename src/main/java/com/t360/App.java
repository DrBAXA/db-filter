package com.t360;

import com.t360.database.DatabaseManager;
import com.t360.filtering.core.PredicateValueDescriptor;
import com.t360.filtering.core.QueryNode;
import com.t360.filtering.core.parsing.QueryParser;
import com.t360.filtering.core.parsing.QueryTreeParsingService;
import com.t360.filtering.tables.ColumnDescription;
import com.t360.filtering.tables.MidMatchStrategy;
import com.t360.filtering.tables.Negotiation;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class App {

    public static void main(String[] args) throws SQLException {
        String json = "{\"expression\":\"A&B\",\"predicates\":{\"A\":{\"field\":\"Currency1\",\"value\":\"UAH\",\"comparingOperator\":\"=\"},\"B\":{\"field\":\"Size1\",\"value\":1000000000000000000000.2,\"comparingOperator\":\"<\"}}}";
        executeJsonQuery(json, Negotiation.class);

        String json2 = "{\"expression\":\"A\",\"predicates\":{\"A\":{\"field\":\"Symbol\",\"value\":\"AA\",\"comparingOperator\":\"=\"}}}";
        executeJsonQuery(json2, MidMatchStrategy.class);

        String json3 = "{\"expression\":\"A&(B|C)\",\"predicates\":{\"A\":{\"field\":\"Size1\",\"value\":100,\"comparingOperator\":\">=\"},\"B\":{\"field\":\"Size2\",\"value\":10000000,\"comparingOperator\":\"<\"},\"C\":{\"field\":\"Currency1\",\"value\":[\"UAH\",\"EUR\"],\"comparingOperator\":\"IN\"}}}";
        executeJsonQuery(json3, Negotiation.class);
    }

    private static <T, F extends Enum<F> & ColumnDescription<T>> void executeJsonQuery(String jsonInput, Class<F> tableEnum) throws SQLException {
        QueryTreeParsingService parsingService = new QueryParser();
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
                    Class<?> valueClass = valHolder.getValue().getClass();

                    if (valueClass.equals(BigDecimal.class)) {
                        ps.setBigDecimal(index++, (BigDecimal) valHolder.getValue());
                    } else if (valueClass.equals(String.class)) {
                        ps.setString(index++, (String) valHolder.getValue());
                    } else if (valueClass.equals(Boolean.class)) {
                        ps.setBoolean(index++, (Boolean) valHolder.getValue());
                    } else if (valHolder.getValue() instanceof Collection) {
                        // todo check if array works on other jdbc vendor
//                        Object[] array = ((Collection<?>) valHolder.getValue()).toArray();
//                        String typeName = resolveJdbcType(array[0]);
//                        Array sqlArray = ps.getConnection().createArrayOf(typeName, array);
//                        ps.setArray(index++, sqlArray);
                        Iterator<?> it = ((Collection<?>) valHolder.getValue()).iterator();
                        while (it.hasNext()) {
                            if (valHolder.getFieldType().equals(BigDecimal.class)) {
                                ps.setBigDecimal(index++, (BigDecimal) it.next());
                            } else if (valHolder.getFieldType().equals(String.class)) {
                                ps.setString(index++, (String) it.next());
                            } else if (valHolder.getFieldType().equals(Boolean.class)) {
                                ps.setBoolean(index++, (Boolean) it.next());
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalStateException("Cant correctly configure prepare statement: SQL State " + e.getSQLState());
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