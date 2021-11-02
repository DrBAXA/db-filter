package com.t360.filtering.tables;

import lombok.Getter;

/*
 * TODO enum naming
 */
public enum Negotiation {

    Id("ID"),
    Product("PRODUCT"),
    Currency1("CURRENCY1"),
    Side("SIDE"),
    Date1("DATE1"),
    Size1("SIZE1"),
    Size2("SIZE2"),
    AggressiveCompany("AGGRESSIVE_COMPANY"),
    PassiveCompany("PASSIVE_COMPANY"),
    FidmId("FIDM_ID"),
    Time1("TIME1"),
    Price1("PRICE1");

    Negotiation(String cn) {
        this.columnName = cn;
    }

    @Getter
    private final String columnName;


}
