package com.t360.external.database;

import com.t360.external.entities.MidMatchStrategyRow;
import com.t360.external.entities.NegotiationRow;
import com.t360.filtering.tables.MidMatchStrategy;
import com.t360.filtering.tables.Negotiation;
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
            sf.setConnection(DriverManager.getConnection(connectionURL, "SA", ""));
            sf.execute();
        } catch (IOException | SqlToolError | SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> execute(String query, Consumer<PreparedStatement> consumer) throws SQLException {
        try (Connection connection = DriverManager.getConnection(connectionURL, "SA", "");
             PreparedStatement ps = connection.prepareStatement(query)) {

            // enrich statement values if such exist
            Optional.ofNullable(consumer).ifPresent(c -> c.accept(ps));
            List<String> lines = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            int columnCount = rs.getMetaData().getColumnCount();

            // Column names in first row
            lines.add(joinValues(rs.getMetaData()::getColumnName, columnCount));

            // column values
            while (rs.next()) {
                lines.add(joinValues(rs::getString, columnCount));
            }
            return lines;
        }
    }

    private static String joinValues(ValueExtractor valueByIndex, int columnCount) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            sb.append(valueByIndex.getByIndex(i)).append("\t");
        }
        return sb.toString();
    }

    @FunctionalInterface
    private interface ValueExtractor {
        String getByIndex(int index) throws SQLException;
    }

}
