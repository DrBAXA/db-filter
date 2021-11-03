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