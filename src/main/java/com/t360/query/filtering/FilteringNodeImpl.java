package com.t360.query.filtering;

import com.t360.query.filtering.tree.TreeNode;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class FilteringNodeImpl<T> implements FilteringNode<T> {

	private final TreeNode<T> root;

	public FilteringNodeImpl(TreeNode<T> root) {
		this.root = root;
	}

	@Override
	public String asSqlWhereClause() {
		return root.asSqlWhereClause();
	}

	@Override
	public void fillPreparedStatement(PreparedStatement ps) {
		try {
			int index = 1;
			for (PredicateValueDescriptor valHolder : collectPredicates()) {
				Class<?> valueClass = valHolder.getValue().getClass();
				if (valHolder.getValue() instanceof Collection) {
					for (Object o : (Collection<?>) valHolder.getValue())
						resolveAndSetField(ps, index++, valHolder.getFieldType(), o);
				} else
					resolveAndSetField(ps, index++, valueClass, valHolder.getValue());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("Cant correctly configure prepare statement: SQL State " + e.getSQLState());
		}
	}

	private void resolveAndSetField(PreparedStatement ps, int index, Class<?> valueClass, Object val) throws SQLException {
		if (valueClass.equals(BigDecimal.class)) {
			ps.setBigDecimal(index, (BigDecimal) val);
		} else if (valueClass.equals(String.class)) {
			ps.setString(index, (String) val);
		} else if (valueClass.equals(Boolean.class)) {
			ps.setBoolean(index, (Boolean) val);
		} else if (valueClass.equals(Byte.class)) {
			ps.setByte(index, (Byte) val);
		} else if (valueClass.equals(Short.class)) {
			ps.setShort(index, (Short) val);
		} else if (valueClass.equals(Integer.class)) {
			ps.setInt(index, (Integer) val);
		} else if (valueClass.equals(Long.class)) {
			ps.setLong(index, (Long) val);
		}
	}

	@Override
	public List<PredicateValueDescriptor> collectPredicates() {
		return root.collectPredicates();
	}

	@Override
	public Predicate<T> generateJavaPredicate() {
		return root.generateJavaPredicate();
	}
}
