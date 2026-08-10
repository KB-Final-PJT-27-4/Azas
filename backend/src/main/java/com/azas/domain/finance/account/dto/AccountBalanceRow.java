package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AccountBalanceRow {

    private Long accountId;
    private String ownerType;
    private Long connectedByMemberId;
    private Long childId;
    private BigDecimal balance;
    private LocalDateTime balanceUpdatedAt;
}
