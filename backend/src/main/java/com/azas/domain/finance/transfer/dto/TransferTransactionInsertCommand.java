package com.azas.domain.finance.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TransferTransactionInsertCommand {

    private Long accountTransactionId;
    private final Long financialAccountId;
    private final Long childId;
    private final String transactionFingerprint;
    private final LocalDateTime occurredAt;
    private final String direction;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String description;
    private final String counterpartyName;
}