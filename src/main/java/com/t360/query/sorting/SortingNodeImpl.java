package com.t360.query.sorting;

import com.t360.query.filtering.ColumnDescription;
import lombok.Value;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
public class SortingNodeImpl<T, F extends ColumnDescription<T>> implements SortingNode<T> {

	public static final Comparator<?> IDENTITY_COMPARATOR = (any1, any2) -> 0;

	/*
	 * Identity comparator - always returns 0 so doesn't change the order of objects
	 */
	@SuppressWarnings("unchecked")
	private static <T> Comparator<T> identity() {
		return (Comparator<T>) IDENTITY_COMPARATOR;
	}

	List<Order<T, F>> order;

	@Override
	public String asSqlOrderByClause() {
		return order.stream()
				.map(Order::asSqlOrderByClause)
				.collect(Collectors.joining(", "));
	}

	@Override
	public Comparator<T> generateComparator() {
		return order.stream()
				.map(Order::asComparator)
				.reduce(Comparator::thenComparing)
				.orElse(identity());
	}

	@Value
	public static class Order<T, F extends ColumnDescription<T>> {

		F field;

		Direction direction;

		/*
		 * Used raw types here as we can't in Java have a separate types for different enum values
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Comparator<T> asComparator() {
			Comparator<T> fieldComparator = Comparator.comparing(v -> {
				Function<T, ? extends Comparable<?>> fieldAccessor = field.getFieldAccessor();
				Comparable<?> fieldValue = fieldAccessor.apply(v);
				return (Comparable) fieldValue;
			});

			Comparator<T> nullSafeComparator = Comparator.nullsFirst(fieldComparator);

			return direction == Direction.DESC ? nullSafeComparator.reversed() : nullSafeComparator;
		}

		public String asSqlOrderByClause() {
			return String.format("%s %s", field.getColumnName(), direction);
		}
	}

	public enum Direction {
		ASC, DESC
	}

}
