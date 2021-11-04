package com.t360.database;

import com.t360.filtering.core.QueryNode;
import com.t360.filtering.tables.TableQuery;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Queries database with
 */
@Slf4j
public class DatabaseQueryService {

    private final String connectionURL = "jdbc:hsqldb:file:test.db/";

    public DatabaseQueryService() {
    }

    public String[][] query(TableQuery tableQuery, QueryNode<?> filterQuery) {
        List<String[]> result = new ArrayList<>();
        final String sql = getSQL(tableQuery, filterQuery);
        log.debug("Executing query " + sql);
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            int columnCount = resultSet.getMetaData().getColumnCount();

            while (resultSet.next()) {
                String[] rowResult = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    rowResult[i] = resultSet.getString(i + 1);
                }
                result.add(rowResult);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result.toArray(new String[0][]);
    }

    private String getSQL(TableQuery tableQuery, QueryNode<?> filterQuery) {
        StringBuilder queryBuilder = tableQuery.selectAllQuery().append(" WHERE ");
        final int initialLength = queryBuilder.length();
        filterQuery.appendWhereClause(queryBuilder);
        if (queryBuilder.length() == initialLength) {
            queryBuilder.delete(initialLength - 7, initialLength);
        }
        return queryBuilder.toString();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionURL, "SA", "");
    }

}
