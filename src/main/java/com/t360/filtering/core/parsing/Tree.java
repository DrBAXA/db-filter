package com.t360.filtering.core.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.t360.filtering.core.Operator;
import lombok.Data;

import java.util.List;

@Data
public final class Tree implements JsonQuery {

    @JsonProperty
    Operator operator = Operator.AND;

    @JsonProperty
    List<JsonQuery> predicates;
}
