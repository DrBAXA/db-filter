package com.t360.external.entities;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NegotiationRow {
    private long id;
    private String product;
    private String currency1;
    private byte side;
    private int date1;
    private BigDecimal size1;
    private BigDecimal size2;
    private long aggressiveCompany;
    private long passiveCompany;
    private long fidmId;
    private long time1;
    private BigDecimal price1;
}
