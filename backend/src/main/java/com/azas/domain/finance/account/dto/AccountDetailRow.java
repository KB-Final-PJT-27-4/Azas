package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AccountDetailRow {

    private Long accountId;
    private String ownerType;
    private Long ownerMemberId;
    private Long childId;
    private String childName;
    private String organizationCode;
    private String bankName;
    private String accountName;
    private byte[] accountNumberCiphertext;
    private String accountProductType;
    private BigDecimal balance;
    private LocalDateTime balanceUpdatedAt;
    private String accountStatus;
    private boolean primaryAccount;
    private LocalDateTime openedAt;
    private LocalDate maturityDate;
    private LocalDateTime linkedAt;
    private String goalNameSnapshot;
    private BigDecimal goalTargetAmount;
    private LocalDate goalTargetDate;
}
