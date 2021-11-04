package com.t360.filtering.core.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.t360.filtering.core.ComparingOperator;
import lombok.Data;

@Data
public final class JsonPredicate implements JsonQuery {

    @JsonProperty
    String field;
    @JsonProperty
    String value;
    @JsonProperty
    ComparingOperator comparingOperator;

}
