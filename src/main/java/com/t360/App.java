package com.t360;

import com.t360.database.DatabaseQueryService;
import com.t360.filtering.core.parsing.FlatTreeParsingService;
import com.t360.filtering.core.parsing.ParsingException;
import com.t360.filtering.core.parsing.QueryTreeParsingService;


public class App {

    public static void main(String[] args) throws ParsingException {

        QueryTreeParsingService queryTreeParsingService = new FlatTreeParsingService();
        DatabaseQueryService databaseQueryService = new DatabaseQueryService();

        TableDataController tableDataController = new TableDataController(queryTreeParsingService, databaseQueryService);

        final String[][] negotiationTableData = tableDataController.getNegotiationTableData("{\n" +
                "                  \"predicates\": [\n" +
                "                    {\n" +
                "                      \"field\": \"Currency1\",\n" +
                "                      \"value\": \"UAH\",\n" +
                "                      \"comparingOperator\": \"<\"\n" +
                "                    }\n" +
                "                  ]\n" +
                "                }");

        for (String[] negotiationTableRow : negotiationTableData) {
            for (String column : negotiationTableRow) {
                System.out.print(column + "\t");
            }
            System.out.println();
        }

    }
}
