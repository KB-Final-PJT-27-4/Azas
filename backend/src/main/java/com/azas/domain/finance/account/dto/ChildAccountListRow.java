package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChildAccountListRow {

    private Long accountId;
    private String organizationCode;
    private String bankName;
    private String accountName;
    private byte[] accountNumberCiphertext;
    private String accountProductType;
    private BigDecimal balance;
    private LocalDateTime balanceUpdatedAt;
    private String accountStatus;
    private boolean primaryAccount;
}
