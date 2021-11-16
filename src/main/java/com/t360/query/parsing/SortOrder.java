package com.t360.query.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.t360.query.sorting.SortingNodeImpl;
import lombok.Data;

@Data
public class SortOrder {

	@JsonProperty
	private String field;

	@JsonProperty
	private SortingNodeImpl.Direction direction;

}
