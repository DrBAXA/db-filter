package com.t360.filtering.tables;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MidMatchStrategyRow {

       private Long id;
       private Long negotiation_id;
       private String symbol;
       private Integer strategy_type;
       private Integer strategy_source;
       private String near_leg_tenor;
       private String far_leg_tenor;
       private Boolean side;
       private Integer owner;
       private BigDecimal spot_sensitivity_price;
       private Boolean is_partial_fill_allowed;
       private Boolean is_base_notional;
       private Boolean is_market_maker_agreement;
}
