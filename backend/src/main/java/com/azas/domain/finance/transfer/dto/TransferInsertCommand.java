package com.azas.domain.finance.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TransferInsertCommand {

    private Long financialTransferId;
    private final Long childId;
    private final Long requestedByMemberId;
    private final Long sourceAccountId;
    private final Long destinationAccountId;
    private final BigDecimal amount;
    private final String memo;
    private final String idempotencyKey;
    private final LocalDateTime requestedAt;
}