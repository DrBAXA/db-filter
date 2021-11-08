package com.t360.filtering.core;

import com.t360.filtering.core.tree.TreeNode;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class QueryNodeImpl<T> implements QueryNode<T> {


    private final TreeNode<T> root;

    public QueryNodeImpl(TreeNode<T> root) {
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

                if (valueClass.equals(BigDecimal.class)) {
                    ps.setBigDecimal(index++, (BigDecimal) valHolder.getValue());
                } else if (valueClass.equals(String.class)) {
                    ps.setString(index++, (String) valHolder.getValue());
                } else if (valueClass.equals(Boolean.class)) {
                    ps.setBoolean(index++, (Boolean) valHolder.getValue());
                } else if (valueClass.equals(Byte.class)) {
                    ps.setByte(index++, (Byte) valHolder.getValue());
                } else if (valueClass.equals(Short.class)) {
                    ps.setShort(index++, (Short) valHolder.getValue());
                } else if (valueClass.equals(Integer.class)) {
                    ps.setInt(index++, (Integer) valHolder.getValue());
                } else if (valueClass.equals(Long.class)) {
                    ps.setLong(index++, (Long) valHolder.getValue());
                } else if (valHolder.getValue() instanceof Collection) {
                    // todo check if array works on other jdbc vendors
//                    Object[] array = ((Collection<?>) valHolder.getValue()).toArray();
//                    String typeName = resolveJdbcType(array[0]);
//                    Array sqlArray = ps.getConnection().createArrayOf(typeName, array);
//                    ps.setArray(index++, sqlArray);
                    Iterator<?> it = ((Collection<?>) valHolder.getValue()).iterator();
                    while (it.hasNext()) {
                        if (valHolder.getFieldType().equals(BigDecimal.class)) {
                            ps.setBigDecimal(index++, (BigDecimal) it.next());
                        } else if (valHolder.getFieldType().equals(String.class)) {
                            ps.setString(index++, (String) it.next());
                        } else if (valHolder.getFieldType().equals(Integer.class)) {
                            ps.setInt(index++, (Integer) it.next());
                        } else if (valHolder.getFieldType().equals(Long.class)) {
                            ps.setLong(index++, (Long) it.next());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Cant correctly configure prepare statement: SQL State " + e.getSQLState());
        }
    }

    private String resolveJdbcType(Object obj) {
        if (obj instanceof String) return JDBCType.VARCHAR.getName();
        else if (obj instanceof Long) return JDBCType.BIGINT.getName();
        else if (obj instanceof Integer) return JDBCType.INTEGER.getName();
        else if (obj instanceof BigDecimal) return JDBCType.DECIMAL.getName();
        else throw new IllegalStateException("Does not support '" + obj.getClass().getSimpleName() + "' type yet");
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
