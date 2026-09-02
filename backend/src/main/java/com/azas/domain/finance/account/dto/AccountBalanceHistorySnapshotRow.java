package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AccountBalanceHistorySnapshotRow {

    private BigDecimal balance;
    private LocalDateTime observedAt;
}
