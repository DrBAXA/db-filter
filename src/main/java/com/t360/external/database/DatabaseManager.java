package com.t360.external.database;

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

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionURL, "SA", "");
    }

}
