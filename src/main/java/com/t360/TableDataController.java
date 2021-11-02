package com.t360;

import com.t360.filtering.core.QueryNode;
import com.t360.database.DatabaseQueryService;
import com.t360.filtering.core.parsing.ParsingException;
import com.t360.filtering.core.parsing.QueryTreeParsingService;

public class TableDataController {

    private final QueryTreeParsingService queryTreeParsingService;
    private final DatabaseQueryService databaseQueryService;

    public TableDataController(QueryTreeParsingService queryTreeParsingService,
                               DatabaseQueryService databaseQueryService) {
        this.queryTreeParsingService = queryTreeParsingService;
        this.databaseQueryService = databaseQueryService;
    }

    public String[][] getNegotiationTableData(String jsonQuery) throws ParsingException {
        QueryNode query = queryTreeParsingService.parse(jsonQuery);
        return databaseQueryService.query(() -> new StringBuilder("SELECT * FROM NEGOTIATION"), query);
    }
}
