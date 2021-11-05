package com.t360.filtering.tables;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.function.Function;

/*
 * TODO enum naming
 */
public enum MidMatchStrategy implements ColumnDescription<MidMatchStrategyRow> {

    Id("ID", Long.class, MidMatchStrategyRow::getId),
    Negotiation_id("NEGOTIATION_ID", Long.class, MidMatchStrategyRow::getNegotiation_id),
    Symbol("SYMBOL", String.class, MidMatchStrategyRow::getSymbol),
    Strategy_type("STRATEGY_TYPE", Integer.class, MidMatchStrategyRow::getStrategy_type),
    Strategy_source("STRATEGY_SOURCE", Integer.class, MidMatchStrategyRow::getStrategy_source),
    Near_leg_tenor("NEAR_LEG_TENOR", String.class, MidMatchStrategyRow::getNear_leg_tenor),
    Far_leg_tenor("FAR_LEG_TENOR", String.class, MidMatchStrategyRow::getFar_leg_tenor),
    Side("SIDE", Boolean.class, MidMatchStrategyRow::getSide),
    Owner("OWNER", Integer.class, MidMatchStrategyRow::getOwner),
    Spot_sensitivity_price("SPOT_SENSITIVITY_PRICE", BigDecimal.class, MidMatchStrategyRow::getSpot_sensitivity_price),
    Is_partial_fill_allowed("IS_PARTIAL_FILL_ALLOWED", Boolean.class, MidMatchStrategyRow::getIs_partial_fill_allowed),
    Is_base_notional("IS_BASE_NOTIONAL", Boolean.class, MidMatchStrategyRow::getIs_base_notional),
    Is_market_maker_agreement("IS_MARKET_MAKER_AGREEMENT", Boolean.class, MidMatchStrategyRow::getIs_market_maker_agreement);


    @Getter
    private final String columnName;
    @Getter
    private final Class<?> fieldType;
    @Getter
    private final Function<MidMatchStrategyRow, ? extends Comparable<?>> fieldAccessor;

    MidMatchStrategy(String cn, Class<?> fieldType, Function<MidMatchStrategyRow, ? extends Comparable<?>> fieldAccessor) {
        this.columnName = cn;
        this.fieldType = fieldType;
        this.fieldAccessor = fieldAccessor;
    }

}
