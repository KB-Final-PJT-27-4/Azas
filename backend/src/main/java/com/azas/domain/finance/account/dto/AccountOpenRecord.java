package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class AccountOpenRecord {
    @Setter
    private Long accountId;
    private final String ownerType;
    private final Long ownerMemberId;
    private final Long childId;
    private final Long financialProductId;
    private final String bankName;
    private final byte[] accountNumberCiphertext;
    private final String accountNumberHash;
    private final String accountName;
    private final String accountProductType;
    private final BigDecimal balance;
    private final boolean primaryAccount;
    private final LocalDateTime createdAt;
    private final LocalDate maturityDate;
}
