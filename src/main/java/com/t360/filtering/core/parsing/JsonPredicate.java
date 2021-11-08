package com.t360.filtering.core.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.t360.filtering.core.ComparingOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class JsonPredicate {

    @JsonProperty
    String field;
    @JsonProperty
    @JsonDeserialize(using = DumbStringDeserializer.class)
    String value;
    @JsonProperty
    ComparingOperator operator;

}
