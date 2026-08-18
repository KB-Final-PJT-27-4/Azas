package com.azas.domain.timecapsule.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TimeCapsuleEntryTransaction {

    private Long accountTransactionId;
    private AccountTransactionDirection direction;
    private BigDecimal amount;
    private LocalDateTime occurredAt;

    public boolean isCredit() {
        return direction == AccountTransactionDirection.CREDIT;
    }

    public boolean hasPositiveAmount() {
        return amount != null && amount.signum() > 0;
    }
}
