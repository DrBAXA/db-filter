package com.t360.filtering.core.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.t360.filtering.core.LogicalOperator;
import lombok.Data;

import java.util.List;

@Data
public final class Tree implements JsonQuery {

    @JsonProperty
    LogicalOperator operator = LogicalOperator.AND;

    @JsonProperty
    List<JsonQuery> predicates;
}
