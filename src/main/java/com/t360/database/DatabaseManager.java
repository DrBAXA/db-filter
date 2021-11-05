package com.t360.database;

import com.t360.filtering.core.QueryNode;
import com.t360.filtering.tables.TableQuery;
import lombok.extern.slf4j.Slf4j;
import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Queries database with
 */
@Slf4j
public class DatabaseManager {

    private static final String connectionURL = "jdbc:hsqldb:mem:test.db";

    static {
        try {
            SqlFile sf = new SqlFile(new File("HSQL_Setup.sql"));
            sf.setConnection(getConnection());
            sf.execute();
        } catch (IOException | SqlToolError | SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> execute(String query, Consumer<PreparedStatement> consumer) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            // enrich statement values if such exist
            Optional.ofNullable(consumer).ifPresent(c -> c.accept(ps));
            List<String> lines = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < columnCount; i++) {
                    sb.append(rs.getString(i)).append("\t");
                }
                lines.add(sb.toString());
            }
            return lines;
        }
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

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionURL, "SA", "");
    }

}
