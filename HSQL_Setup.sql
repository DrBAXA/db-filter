CREATE TABLE NEGOTIATION
(
    ID                 BIGINT      NOT NULL,
    PRODUCT            VARCHAR(32) NOT NULL,
    CURRENCY1          VARCHAR(3),
    SIDE               BIT,
    DATE1              INT,
    SIZE1              DECIMAL(35, 15),
    SIZE2              DECIMAL(35, 15),
    AGGRESSIVE_COMPANY BIGINT,
    PASSIVE_COMPANY    BIGINT,
    FIDM_ID            BIGINT,
    HEDGE_FOR_FIDM_ID  BIGINT,
    TIME1              BIGINT,
    PRICE1             DECIMAL(35, 15),
    CONSTRAINT NEGOTIATION_PK PRIMARY KEY (ID)
);

INSERT INTO NEGOTIATION(ID, PRODUCT, CURRENCY1, SIDE, DATE1, SIZE1, SIZE2,
                        AGGRESSIVE_COMPANY, PASSIVE_COMPANY, FIDM_ID,
                        HEDGE_FOR_FIDM_ID, TIME1, PRICE1)
VALUES (1, 'TEST', 'UAH', 1, 123456789, 10000000.00, 1000000.00, 1, 2, 1, 1, 123456789, 10000000.00),
       (2, 'TEST2', 'EUR', 1, 125456789, 100.00, 1000.00, 1, 2, 1, 1, 125456789, 1000.00);

CREATE TABLE MID_MATCH_STRATEGY
(
    "ID"                        BIGINT        NOT NULL,
    "NEGOTIATION_ID"            BIGINT        NOT NULL,
    "SYMBOL"                    VARCHAR(6)    NOT NULL,
    "STRATEGY_TYPE"             INT           NOT NULL,
    "STRATEGY_SOURCE"           INT           NOT NULL,
    "NEAR_LEG_TENOR"            VARCHAR(10)   NOT NULL,
    "FAR_LEG_TENOR"             VARCHAR(10)   NOT NULL,
    "SIDE"                      BIT           NOT NULL,
    "OWNER"                     BIGINT        NOT NULL,
    "SPOT_SENSITIVITY_PRICE"    DECIMAL(18, 12),
    "IS_PARTIAL_FILL_ALLOWED"   BIT DEFAULT 1 NOT NULL,
    "IS_BASE_NOTIONAL"          BIT DEFAULT 1 NOT NULL,
    "IS_MARKET_MAKER_AGREEMENT" BIT,
    PRIMARY KEY ("ID")
);

INSERT INTO MID_MATCH_STRATEGY(ID, NEGOTIATION_ID, SYMBOL, STRATEGY_TYPE, STRATEGY_SOURCE,
                               NEAR_LEG_TENOR, FAR_LEG_TENOR, SIDE, OWNER, SPOT_SENSITIVITY_PRICE,
                               IS_PARTIAL_FILL_ALLOWED, IS_BASE_NOTIONAL, IS_MARKET_MAKER_AGREEMENT)
VALUES (1, 1, 'AA', 100, 1, 'TEST_100', 'SMSELSE', 1, 1, 110.27, 1, 1, 1),
       (2, 2, 'AA', 100, 2, 'TEST_101', 'SMSELSE2', 1, 1, 98.14, 1, 0, 0),
       (3, 2, 'AA', 200, 3, 'TEST_102', 'SMSELSE2', 1, 1, 557.48, 0, 1, 0),

       (4, 2, 'BB', 100, 1, 'TEST_103', 'SMSELSE2', 1, 1, 53.81, 1, 1, 0),
       (5, 2, 'BB', 200, 2, 'TEST_104', 'SMSELSE2', 1, 1, 127.17, 0, 1, 1),
       (6, 2, 'BB', 300, 3, 'TEST_105', 'SMSELSE2', 1, 1, 76.34, 1, 0, 0),

       (7, 2, 'CC', 100, 1, 'TEST_106', 'SMSELSE2', 1, 1, 96.23, 0, 1, 0),
       (8, 2, 'CC', 200, 2, 'TEST_107', 'SMSELSE2', 1, 1, 108.69, 1, 1, 0),
       (9, 2, 'CC', 300, 3, 'TEST_108', 'SMSELSE2', 1, 1, 88.93, 1, 0, 1),

       (10, 2, 'DD', 200, 1, 'TEST_109', 'SMSELSE2', 1, 1, 134.27, 0, 0, 0),
       (11, 2, 'DD', 400, 2, 'TEST_110', 'SMSELSE2', 1, 1, 147.21, 0, 1, 0),
       (12, 2, 'DD', 600, 3, 'TEST_111', 'SMSELSE2', 1, 1, 82.46, 0, 1, 1),
       (13, 2, 'DD', 800, 4, 'TEST_112', 'SMSELSE2', 1, 1, 88.74, 1, 0, 0),
       (14, 2, 'DD', 1000, 5, 'TEST_113', 'SMSELSE2', 1, 1, 92.94, 1, 1, 1),
       (15, 2, 'DD', 1200, 6, 'TEST_114', 'SMSELSE2', 1, 1, 119.61, 1, 1, 0);
