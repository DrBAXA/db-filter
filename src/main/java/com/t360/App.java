package com.t360;

import com.t360.external.json.JsonParsingUtil;
import com.t360.filtering.core.PredicateValueDescriptor;
import com.t360.filtering.core.QueryNode;
import com.t360.filtering.core.parsing.JsonQuery;
import com.t360.filtering.core.parsing.QueryParser;
import com.t360.filtering.core.QueryTreeParsingService;
import com.t360.filtering.core.ColumnDescription;
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
        String json;

        // Currency1 = USD
        json = "{\"expression\":\"A\",\"predicates\":{\"A\":{\"field\":\"Currency1\",\"value\":\"USD\",\"operator\":\"=\"}}}";
        executeJsonQuery("List all USD currencies", json, Negotiation.class);

        // Currency1 = USD and AggressiveCompany = 10030
        json = "{\"expression\":\"A&B\",\"predicates\":{\"A\":{\"field\":\"Currency1\",\"value\":\"USD\",\"operator\":\"=\"},\"B\":{\"field\":\"AggressiveCompany\",\"value\":10030,\"operator\":\">\"}}}";
        executeJsonQuery("All USD currencies and aggressiveCompany > 10030", json, Negotiation.class);

        // Currency1 = USD and
        // AggressiveCompany = 10030 or PassiveCompany IN (232929, 232928, 232927, 232926)
        json = "{\"expression\":\"A&(B|C)\",\"predicates\":{\"A\":{\"field\":\"Currency1\",\"value\":\"USD\",\"operator\":\"=\"},\"B\":{\"field\":\"AggressiveCompany\",\"value\":10030,\"operator\":\">\"},\"C\":{\"field\":\"PassiveCompany\",\"value\":[232929, 232928, 232927, 232926],\"operator\":\"IN\"}}}";
        executeJsonQuery("All USD and (aggressiveCompany > 10030 OR passiveCompany in (232929, 232928, 232927, 232926) )", json, Negotiation.class);

        // SYMBOL NOT IN [AA, DD] and SPOT_SENSITIVITY_PRICE IS NOT NULL and STRATEGY_TYPE < 1300
        // OR
        // SYMBOL IN [CC, DD] and STRATEGY_TYPE > 2000
        json = "{\"expression\":\"(A&B&C)|(D&E)\",\"predicates\":{\"A\":{\"field\":\"Symbol\",\"value\":[\"AA\", \"DD\"],\"operator\":\"NOT IN\"}, \"B\":{\"field\":\"Spot_sensitivity_price\",\"operator\":\"IS NOT NULL\"}, \"C\":{\"field\":\"Strategy_type\",\"value\":1300,\"operator\":\"<\"}, \"D\":{\"field\":\"Symbol\",\"value\":[\"CC\", \"DD\"],\"operator\":\"IN\"}, \"E\":{\"field\":\"Strategy_type\",\"value\":2000,\"operator\":\">=\"}}}";
        executeJsonQuery("Complex query to second table", json, MidMatchStrategy.class);
    }

    private static <T, F extends Enum<F> & ColumnDescription<T>> void executeJsonQuery(String testName, String jsonInput, Class<F> tableEnum) throws SQLException {
        QueryTreeParsingService parsingService = new QueryParser();

        final JsonQuery jsonQuery = JsonParsingUtil.parseJson(jsonInput);

        QueryNode<T> rootNode = parsingService.parse(jsonQuery, tableEnum);
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ")
                .append(resolveTableName(tableEnum))
                .append(" WHERE ")
                .append(rootNode.asSqlWhereClause());

        System.out.println("\nTEST: " + testName + "\n\t" + sqlQuery + "\n");

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
                    } else if (valueClass.equals(Byte.class)) {
                        ps.setByte(index++, (Byte) valHolder.getValue());
                    } else if (valueClass.equals(Short.class)) {
                        ps.setShort(index++, (Short) valHolder.getValue());
                    } else if (valueClass.equals(Integer.class)) {
                        ps.setInt(index++, (Integer) valHolder.getValue());
                    } else if (valueClass.equals(Long.class)) {
                        ps.setLong(index++, (Long) valHolder.getValue());
                    } else if (valHolder.getValue() instanceof Collection) {
                        // todo check if array works on other jdbc vendors
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
                            } else if (valHolder.getFieldType().equals(Integer.class)) {
                                ps.setInt(index++, (Integer) it.next());
                            } else if (valHolder.getFieldType().equals(Long.class)) {
                                ps.setLong(index++, (Long) it.next());
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalStateException("Cant correctly configure prepare statement: SQL State " + e.getSQLState());
            }
        };
        List<String> result = com.t360.external.database.DatabaseManager.execute(sqlQuery.toString(), applyFunction);
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