package com.t360.filtering.tables;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.function.Function;

/*
 * TODO enum naming
 */
public enum Negotiation implements ColumnDescription<NegotiationRow> {

    Id("ID", Long.class, NegotiationRow::getId),
    Product("PRODUCT", String.class, NegotiationRow::getProduct),
    Currency1("CURRENCY1", String.class, NegotiationRow::getCurrency1),
    Side("SIDE", Byte.class, NegotiationRow::getSide),
    Date1("DATE1", Integer.class, NegotiationRow::getDate1),
    Size1("SIZE1", BigDecimal.class, NegotiationRow::getSize1),
    Size2("SIZE2", BigDecimal.class, NegotiationRow::getSize2),
    AggressiveCompany("AGGRESSIVE_COMPANY", Long.class, NegotiationRow::getAggressiveCompany),
    PassiveCompany("PASSIVE_COMPANY", Long.class, NegotiationRow::getPassiveCompany),
    FidmId("FIDM_ID", Long.class, NegotiationRow::getFidmId),
    Time1("TIME1", Long.class, NegotiationRow::getTime1),
    Price1("PRICE1", BigDecimal.class, NegotiationRow::getPrice1);

    Negotiation(String cn, Class<?> fieldType, Function<NegotiationRow, ?> fieldAccessor) {
        this.columnName = cn;
        this.fieldType = fieldType;
        this.fieldAccessor = fieldAccessor;
    }

    @Getter
    private final String columnName;
    @Getter
    private final Class<?> fieldType;
    private final Function<NegotiationRow, ?> fieldAccessor;

    @Override
    public Function<NegotiationRow, ?> getFieldAccessor() {
        return fieldAccessor;
    }
}
