package com.t360.filtering.core.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.t360.filtering.core.parsing.JsonPredicate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonQuery {

    @JsonProperty
    private String expression;

    @JsonProperty
    private Map<String, JsonPredicate> predicates;

}
