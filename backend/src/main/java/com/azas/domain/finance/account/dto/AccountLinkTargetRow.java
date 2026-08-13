package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AccountLinkTargetRow {
    private Long accountId;
    private String ownerType;
    private Long ownerMemberId;
    private Long childId;
    private String bankName;
    private String accountName;
    private byte[] accountNumberCiphertext;
    private String accountProductType;
    private BigDecimal balance;
    private String accountStatus;
    private String linkStatus;
    private boolean primaryAccount;
    private LocalDateTime linkedAt;
}
