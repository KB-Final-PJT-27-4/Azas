package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AccountDetailRow {

    private Long accountId;
    private String ownerType;
    private Long ownerMemberId;
    private Long childId;
    private String bankName;
    private String accountName;
    private String accountHolderName;
    private byte[] accountNumberCiphertext;
    private String accountProductType;
    private BigDecimal balance;
}
