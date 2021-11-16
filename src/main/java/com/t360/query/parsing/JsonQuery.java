package com.t360.query.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonQuery {

	@JsonProperty
	private String expression;

	@JsonProperty
	private Map<String, JsonPredicate> predicates;

	@JsonProperty
	private List<SortOrder> sorting;

}
