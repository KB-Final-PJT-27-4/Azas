package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class DiscoveredAccountRow {

    private Long accountId;
    private String bankName;
    private String accountName;
    private byte[] accountNumberCiphertext;
    private String accountProductType;
    private BigDecimal balance;
}
