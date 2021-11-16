package com.t360.query.filtering.tree;

import com.t360.external.json.FieldInstantiationUtil;
import com.t360.query.filtering.ColumnDescription;
import com.t360.query.filtering.ComparingOperator;
import com.t360.query.filtering.PredicateValueDescriptor;
import lombok.Value;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a single predicate on a column value
 *
 * @param <T>
 *            type of entity
 * @param <F>
 *            type of table describing enum
 */
@Value
public class ColumnPredicate<T, F extends ColumnDescription<T>> implements TreeNode<T>, PredicateValueDescriptor {

	private static final Set<ComparingOperator> BINARY_OPERATORS = new HashSet<>();

	static {
		BINARY_OPERATORS.add(ComparingOperator.EQUAL);
		BINARY_OPERATORS.add(ComparingOperator.LESS);
		BINARY_OPERATORS.add(ComparingOperator.LESS_OR_EQUAL);
		BINARY_OPERATORS.add(ComparingOperator.GREATER);
		BINARY_OPERATORS.add(ComparingOperator.GREATER_OR_EQUAL);
		BINARY_OPERATORS.add(ComparingOperator.NOT_EQUAL);
	}

	F field;
	Object value;
	ComparingOperator operator;

	public ColumnPredicate(F field, String value, ComparingOperator operator) {
		this.field = field;
		this.value = FieldInstantiationUtil.parseValue(field, operator, value);
		this.operator = operator;
	}

	@Override
	public Predicate<T> generateJavaPredicate() {
		return createPredicate(field, value, operator);
	}

	@Override
	public String asSqlWhereClause() {
		if (BINARY_OPERATORS.contains(operator)) {
			return String.format("%s %s ?", field.getColumnName(), operator.getSqlSign());
		} else {
			if (operator == ComparingOperator.IN || operator == ComparingOperator.NOT_IN) {
				if (value instanceof Collection) {
					String questionMarks = ((Collection<?>) value).stream().map(ignore -> "?")
							.collect(Collectors.joining(", ", "(", ")"));
					return field.getColumnName() + " " + operator.getSqlSign() + " " + questionMarks;
				}
				throw new IllegalStateException("");
			} else {
				return field.getColumnName() + " " + operator.getSqlSign();
			}
		}
	}

	@Override
	public List<PredicateValueDescriptor> collectPredicates() {
		return value == null ? Collections.emptyList() : Collections.singletonList(this);
	}

	private Predicate<T> createPredicate(F tableEnum, Object value, ComparingOperator operator) {
		return entity -> {
			Object fieldValue = tableEnum.getFieldAccessor().apply(entity);

			// Null conditions first
			if (fieldValue == null) {
				return operator == ComparingOperator.IS_NULL;
			} else if (operator == ComparingOperator.IS_NULL || operator == ComparingOperator.IS_NOT_NULL) {
				return operator == ComparingOperator.IS_NOT_NULL;
			}

			// Null is not permitted for predicate object here
			if (value == null) {
				throw new IllegalArgumentException("Null values are not permitted, use IS_NULL or IS_NOT_NULL operators instead.");
			}

			if (operator == ComparingOperator.IN || operator == ComparingOperator.NOT_IN) {
				return handleInClause(fieldValue, value, operator);
			}

			// Values are of the same class and comparable so just compare
			if ((fieldValue.getClass().isInstance(value))) {
				// we already have checked that value is instance of the class of fieldValue
				// noinspection unchecked,rawtypes
				int c = ((Comparable) fieldValue).compareTo(value);
				return examineComparison(operator, c);
			}

			throw new IllegalArgumentException("Wrong data type found");
		};

	}

	private boolean handleInClause(Object fieldValue, Object value, ComparingOperator operator) {
		// must be a collection for IN clause
		if (!(value instanceof Collection)) {
			throw new IllegalArgumentException("List value should be provided for IN and NOT IN clauses.");
		}

		final boolean contains = ((Collection<?>) value).contains(fieldValue);

		// noinspection SimplifiableConditionalExpression
		return operator == ComparingOperator.IN ? contains : !contains;
	}

	private boolean examineComparison(ComparingOperator operator, int c) {
		switch (operator) {
		case LESS_OR_EQUAL:
			return c <= 0;
		case LESS:
			return c < 0;
		case GREATER:
			return c > 0;
		case GREATER_OR_EQUAL:
			return c >= 0;
		case NOT_EQUAL:
			return c != 0;
		case EQUAL:
			return c == 0;
		default:
			return false;
		}
	}

	@Override
	public Class<?> getFieldType() {
		return field.getFieldType();
	}
}
