package com.t360;

import com.t360.filtering.core.QueryNode;
import com.t360.database.DatabaseManager;
import com.t360.filtering.core.parsing.QueryTreeParsingService;
import com.t360.filtering.tables.MidMatchStrategy;
import com.t360.filtering.tables.MidMatchStrategyRow;
import com.t360.filtering.tables.Negotiation;
import com.t360.filtering.tables.NegotiationRow;

public class TableDataController {

    private final QueryTreeParsingService queryTreeParsingService;
    private final DatabaseManager databaseQueryService;

    public TableDataController(QueryTreeParsingService queryTreeParsingService,
                               DatabaseManager databaseManager) {
        this.queryTreeParsingService = queryTreeParsingService;
        this.databaseQueryService = databaseManager;
    }

    public String[][] getNegotiationTableData(String jsonQuery) {
        QueryNode<NegotiationRow> query = queryTreeParsingService.parse(jsonQuery, Negotiation.class);
        return databaseQueryService.query(() -> new StringBuilder("SELECT * FROM NEGOTIATION"), query);
    }

    public String[][] getMidMatchStrategyData(String jsonQuery) {
        QueryNode<MidMatchStrategyRow> query = queryTreeParsingService.parse(jsonQuery, MidMatchStrategy.class);
        return databaseQueryService.query(() -> new StringBuilder("SELECT * FROM MID_MATCH_STRATEGY"), query);
    }
}
