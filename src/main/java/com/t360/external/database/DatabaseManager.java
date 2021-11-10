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

    private static MidMatchStrategyRow parseMidMatchStrategy(ResultSet rs) throws SQLException {
        MidMatchStrategyRow row = new MidMatchStrategyRow();
        row.setId(rs.getLong(MidMatchStrategy.Id.getColumnName()));
        row.setNegotiation_id(rs.getLong(MidMatchStrategy.Negotiation_id.getColumnName()));
        row.setSide(rs.getBoolean(MidMatchStrategy.Side.getColumnName()));
        row.setFar_leg_tenor(rs.getString(MidMatchStrategy.Far_leg_tenor.getColumnName()));
        row.setOwner(rs.getInt(MidMatchStrategy.Owner.getColumnName()));
        row.setStrategy_source(rs.getInt(MidMatchStrategy.Strategy_source.getColumnName()));
        row.setStrategy_type(rs.getInt(MidMatchStrategy.Strategy_type.getColumnName()));
        row.setSpot_sensitivity_price(rs.getBigDecimal(MidMatchStrategy.Spot_sensitivity_price.getColumnName()));
        row.setSymbol(rs.getString(MidMatchStrategy.Symbol.getColumnName()));
        row.setNear_leg_tenor(rs.getString(MidMatchStrategy.Near_leg_tenor.getColumnName()));
        row.setIs_market_maker_agreement(rs.getBoolean(MidMatchStrategy.Is_market_maker_agreement.getColumnName()));
        row.setIs_base_notional(rs.getBoolean(MidMatchStrategy.Is_base_notional.getColumnName()));
        row.setIs_partial_fill_allowed(rs.getBoolean(MidMatchStrategy.Is_partial_fill_allowed.getColumnName()));
        return row;
    }

    private static NegotiationRow parseNegotiation(ResultSet rs) throws SQLException {
        NegotiationRow row = new NegotiationRow();
        row.setId(rs.getLong(Negotiation.Id.getColumnName()));
        row.setFidmId(rs.getLong(Negotiation.FidmId.getColumnName()));
        row.setCurrency1(rs.getString(Negotiation.Currency1.getColumnName()));
        row.setSize1(rs.getBigDecimal(Negotiation.Size1.getColumnName()));
        row.setSize2(rs.getBigDecimal(Negotiation.Size2.getColumnName()));
        row.setDate1(rs.getInt(Negotiation.Date1.getColumnName()));
        row.setAggressiveCompany(rs.getLong(Negotiation.AggressiveCompany.getColumnName()));
        row.setPassiveCompany(rs.getLong(Negotiation.PassiveCompany.getColumnName()));
        row.setProduct(rs.getString(Negotiation.Product.getColumnName()));
        row.setPrice1(rs.getBigDecimal(Negotiation.Price1.getColumnName()));
        row.setTime1(rs.getLong(Negotiation.Time1.getColumnName()));
        row.setSide(rs.getByte(Negotiation.Side.getColumnName()));
        return row;
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
