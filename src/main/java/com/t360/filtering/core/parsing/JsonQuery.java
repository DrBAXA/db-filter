package com.t360.filtering.core.parsing;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(Tree.class),
        @JsonSubTypes.Type(JsonPredicate.class)
})
public interface JsonQuery {


}
