package com.t360;

import com.t360.external.database.DatabaseManager;
import com.t360.external.json.JsonParsingUtil;
import com.t360.filtering.core.ColumnDescription;
import com.t360.filtering.core.QueryNode;
import com.t360.filtering.core.QueryTreeParsingService;
import com.t360.filtering.core.parsing.JsonQuery;
import com.t360.filtering.core.parsing.QueryParser;
import com.t360.filtering.tables.MidMatchStrategy;
import com.t360.filtering.tables.Negotiation;

import java.sql.SQLException;
import java.util.List;

public class App {

    public static void main(String[] args) throws SQLException {
        String json;

        // Currency1 = USD
        json = "{\"expression\":\"A\",\"predicates\":{\"A\":{\"field\":\"Currency1\",\"value\":\"USD\",\"operator\":\"=\"}}}";
        executeJsonQuery("List all USD currencies", json, Negotiation.class);

        // Currency1 = USD and AggressiveCompany > 10030
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



        json = "{\"expression\":\"A\",\"predicates\":{\"A\":{\"field\":\"Is_base_notional\",\"value\":true,\"operator\":\"=\"}}}";
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

        System.out.println("Test case: " + testName + "\n\t" + sqlQuery);

        List<String> result = DatabaseManager.execute(sqlQuery.toString(), rootNode::fillPreparedStatement);
        result.forEach(System.out::println);
        System.out.println("\n");
    }

    private static <T extends Enum<T>> String resolveTableName(Class<T> table) {
        //todo use camelCase to _ transformation if its possible
        if (table.equals(Negotiation.class)) return "NEGOTIATION";
        if (table.equals(MidMatchStrategy.class)) return "MID_MATCH_STRATEGY";
        throw new IllegalStateException(table.getSimpleName() + " table not supported yet");
    }
}