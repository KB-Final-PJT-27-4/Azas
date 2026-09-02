package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AccountTransactionRow {

    private Long accountTransactionId;
    private LocalDateTime occurredAt;
    private String direction;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private String counterpartyName;
    private Long childcareChildId;
    private Long counterpartyAccountId;
    private String counterpartyBankName;
    private String counterpartyAccountName;
    private byte[] counterpartyAccountNumberCiphertext;
}
