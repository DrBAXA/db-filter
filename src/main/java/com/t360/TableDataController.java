package com.t360;

import com.t360.filtering.core.QueryNode;
import com.t360.database.DatabaseQueryService;
import com.t360.filtering.core.parsing.ParsingException;
import com.t360.filtering.core.parsing.QueryTreeParsingService;
import com.t360.filtering.tables.MidMatchStrategy;
import com.t360.filtering.tables.MidMatchStrategyRow;
import com.t360.filtering.tables.Negotiation;
import com.t360.filtering.tables.NegotiationRow;

public class TableDataController {

    private final QueryTreeParsingService queryTreeParsingService;
    private final DatabaseQueryService databaseQueryService;

    public TableDataController(QueryTreeParsingService queryTreeParsingService,
                               DatabaseQueryService databaseQueryService) {
        this.queryTreeParsingService = queryTreeParsingService;
        this.databaseQueryService = databaseQueryService;
    }

    public String[][] getNegotiationTableData(String jsonQuery) throws ParsingException {
        QueryNode<NegotiationRow> query = queryTreeParsingService.parse(jsonQuery, Negotiation.class);
        return databaseQueryService.query(() -> new StringBuilder("SELECT * FROM NEGOTIATION"), query);
    }

    public String[][] getMidMatchStrategyData(String jsonQuery) throws ParsingException {
        QueryNode<MidMatchStrategyRow> query = queryTreeParsingService.parse(jsonQuery, MidMatchStrategy.class);
        return databaseQueryService.query(() -> new StringBuilder("SELECT * FROM MID_MATCH_STRATEGY"), query);
    }
}
