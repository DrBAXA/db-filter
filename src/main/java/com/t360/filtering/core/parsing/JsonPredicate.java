package com.t360.filtering.core.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.t360.filtering.core.ColumnPredicate;
import lombok.Data;

@Data
public final class JsonPredicate implements JsonQuery {

    @JsonProperty
    String field;
    @JsonProperty
    Object value;
    @JsonProperty
    ColumnPredicate.ComparingOperator comparingOperator;

}
