package com.t360.filtering.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.t360.external.entities.MidMatchStrategyRow;
import com.t360.external.entities.NegotiationRow;
import com.t360.filtering.core.parsing.JsonQuery;
import com.t360.filtering.core.parsing.QueryParser;
import com.t360.filtering.tables.MidMatchStrategy;
import com.t360.filtering.tables.Negotiation;
import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegrationTest {

    private static final String GET_NEGOTIATIONS_TEMPLATE = "SELECT * FROM NEGOTIATION";
    private static final String GET_MID_MATCH_STRATEGY_TEMPLATE = "SELECT * FROM MID_MATCH_STRATEGY";

    private QueryParser queryParser = new QueryParser();

    @BeforeAll
    static void setUp() throws SQLException, SqlToolError, IOException {
        try (Connection connection = getConnection()) {
            final URL resource = IntegrationTest.class.getClassLoader().getResource("HSQL_Setup.sql");
            assert resource != null;

            SqlFile sf = new SqlFile(resource);
            sf.setConnection(connection);
            sf.execute();
        }
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        try (Connection connection = getConnection()) {

            connection.prepareCall("DROP TABLE NEGOTIATION").executeUpdate();
            connection.prepareCall("DROP TABLE MID_MATCH_STRATEGY").executeUpdate();
        }
    }

    @Test
    void test_simpleRequest() throws JsonProcessingException, SQLException {
        // Currency1 = USD
        String json = "{\"expression\":\"A\",\"predicates\":{\"A\":{\"field\":\"Currency1\",\"value\":\"USD\",\"operator\":\"=\"}}}";
        final JsonQuery jsonQuery = parseJson(json);

        final QueryNode<NegotiationRow> queryNode = queryParser.parse(jsonQuery, Negotiation.class);

        final List<NegotiationRow> all = query(GET_NEGOTIATIONS_TEMPLATE, any -> {}, IntegrationTest::parseNegotiation);

        final String withSQLPredicate = GET_NEGOTIATIONS_TEMPLATE + " WHERE " + queryNode.asSqlWhereClause();
        final List<NegotiationRow> filteredBySQL = query(withSQLPredicate, queryNode::fillPreparedStatement, IntegrationTest::parseNegotiation);

        final List<NegotiationRow> filteredByPredicate = all.stream().filter(queryNode.generateJavaPredicate()).collect(Collectors.toList());


        assertEquals(12, all.size());
        assertEquals(4, filteredBySQL.size());
        assertEquals(4, filteredByPredicate.size());

        final Set<Long> ids = filteredBySQL.stream().map(NegotiationRow::getId).collect(Collectors.toSet());
        final Set<Long> expectedIDs = new HashSet<>(Arrays.asList(3L, 5L, 8L, 11L));
        assertEquals(expectedIDs, ids);

        assertEquals(filteredBySQL, filteredByPredicate);

    }

    @Test
    void test_simpleAndOperator() throws JsonProcessingException, SQLException {
        // Currency1 = USD and AggressiveCompany > 10030
        String json = "{\"expression\":\"A&B\",\"predicates\":{\"A\":{\"field\":\"Currency1\",\"value\":\"USD\",\"operator\":\"=\"},\"B\":{\"field\":\"AggressiveCompany\",\"value\":10030,\"operator\":\">\"}}}";
        final JsonQuery jsonQuery = parseJson(json);

        final QueryNode<NegotiationRow> queryNode = queryParser.parse(jsonQuery, Negotiation.class);

        final List<NegotiationRow> all = query(GET_NEGOTIATIONS_TEMPLATE, any -> {}, IntegrationTest::parseNegotiation);

        final String withSQLPredicate = GET_NEGOTIATIONS_TEMPLATE + " WHERE " + queryNode.asSqlWhereClause();
        final List<NegotiationRow> filteredBySQL = query(withSQLPredicate, queryNode::fillPreparedStatement, IntegrationTest::parseNegotiation);

        final List<NegotiationRow> filteredByPredicate = all.stream().filter(queryNode.generateJavaPredicate()).collect(Collectors.toList());


        assertEquals(12, all.size());
        assertEquals(2, filteredBySQL.size());
        assertEquals(2, filteredByPredicate.size());

        final Set<Long> ids = filteredBySQL.stream().map(NegotiationRow::getId).collect(Collectors.toSet());
        final Set<Long> expectedIDs = new HashSet<>(Arrays.asList(8L, 11L));
        assertEquals(expectedIDs, ids);

        assertEquals(filteredBySQL, filteredByPredicate);

    }

    @Test
    void test_complex() throws JsonProcessingException, SQLException {
        // Currency1 = USD and
        // AggressiveCompany = 10030 or PassiveCompany IN (232929, 232928, 232927, 232926)
        String json = "{\"expression\":\"A&(B|C)\",\"predicates\":{\"A\":{\"field\":\"Currency1\",\"value\":\"USD\",\"operator\":\"=\"},\"B\":{\"field\":\"AggressiveCompany\",\"value\":10030,\"operator\":\">\"},\"C\":{\"field\":\"PassiveCompany\",\"value\":[232929, 232928, 232927, 232926],\"operator\":\"IN\"}}}";
        final JsonQuery jsonQuery = parseJson(json);

        final QueryNode<NegotiationRow> queryNode = queryParser.parse(jsonQuery, Negotiation.class);

        final List<NegotiationRow> all = query(GET_NEGOTIATIONS_TEMPLATE, any -> {}, IntegrationTest::parseNegotiation);

        final String withSQLPredicate = GET_NEGOTIATIONS_TEMPLATE + " WHERE " + queryNode.asSqlWhereClause();
        final List<NegotiationRow> filteredBySQL = query(withSQLPredicate, queryNode::fillPreparedStatement, IntegrationTest::parseNegotiation);

        final List<NegotiationRow> filteredByPredicate = all.stream().filter(queryNode.generateJavaPredicate()).collect(Collectors.toList());


        assertEquals(12, all.size());
        assertEquals(3, filteredBySQL.size());
        assertEquals(3, filteredByPredicate.size());

        final Set<Long> ids = filteredBySQL.stream().map(NegotiationRow::getId).collect(Collectors.toSet());
        final Set<Long> expectedIDs = new HashSet<>(Arrays.asList(5L, 8L, 11L));
        assertEquals(expectedIDs, ids);

        assertEquals(filteredBySQL, filteredByPredicate);

    }

    @Test
    void test_complex2() throws JsonProcessingException, SQLException {
        // SYMBOL NOT IN [AA, DD] and SPOT_SENSITIVITY_PRICE IS NOT NULL and STRATEGY_TYPE < 1300
        // OR
        // SYMBOL IN [CC, DD] and STRATEGY_TYPE >= 2000
        String json = "{\"expression\":\"(A&B&C)|(D&E)\",\"predicates\":{\"A\":{\"field\":\"Symbol\",\"value\":[\"AA\", \"DD\"],\"operator\":\"NOT IN\"}, \"B\":{\"field\":\"Spot_sensitivity_price\",\"operator\":\"IS NOT NULL\"}, \"C\":{\"field\":\"Strategy_type\",\"value\":1300,\"operator\":\"<\"}, \"D\":{\"field\":\"Symbol\",\"value\":[\"CC\", \"DD\"],\"operator\":\"IN\"}, \"E\":{\"field\":\"Strategy_type\",\"value\":2000,\"operator\":\">=\"}}}";
        final JsonQuery jsonQuery = parseJson(json);

        final QueryNode<MidMatchStrategyRow> queryNode = queryParser.parse(jsonQuery, MidMatchStrategy.class);

        final List<MidMatchStrategyRow> all = query(GET_MID_MATCH_STRATEGY_TEMPLATE, any -> {}, IntegrationTest::parseMidMatchStrategy);

        final String withSQLPredicate = GET_MID_MATCH_STRATEGY_TEMPLATE + " WHERE " + queryNode.asSqlWhereClause();
        final List<MidMatchStrategyRow> filteredBySQL = query(withSQLPredicate, queryNode::fillPreparedStatement, IntegrationTest::parseMidMatchStrategy);

        final List<MidMatchStrategyRow> filteredByPredicate = all.stream().filter(queryNode.generateJavaPredicate()).collect(Collectors.toList());


        assertEquals(15, all.size());
        assertEquals(6, filteredBySQL.size());
        assertEquals(6, filteredByPredicate.size());

        final Set<Long> ids = filteredBySQL.stream().map(MidMatchStrategyRow::getId).collect(Collectors.toSet());
        final Set<Long> expectedIDs = new HashSet<>(Arrays.asList(4L, 5L, 8L, 9L, 14L, 15L));
        assertEquals(expectedIDs, ids);

        assertEquals(filteredBySQL, filteredByPredicate);

    }

    public static <T> List<T> query(String query, Consumer<PreparedStatement> psFiller, ValueExtractor<T> mapper) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            psFiller.accept(ps);

            List<T> lines = new ArrayList<>();
            ResultSet rs = ps.executeQuery();

            // column values
            while (rs.next()) {
                lines.add(mapper.apply(rs));
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

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:mem:test.db", "SA", "");
    }

    public static JsonQuery parseJson(String jsonQuery) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonQuery, JsonQuery.class);
    }

    @FunctionalInterface
    private interface ValueExtractor<T> {
        T apply(ResultSet resultSet) throws SQLException;
    }

}
