package com.t360.query.filtering;

public interface PredicateValueDescriptor {

	Class<?> getFieldType();

	Object getValue();

}
