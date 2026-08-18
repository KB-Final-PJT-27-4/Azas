package com.azas.domain.finance.autotransfer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AutoTransferExecutionAccountRow {

    private Long financialAccountId;
    private String ownerType;
    private Long ownerMemberId;
    private Long childId;
    private String accountName;
    private String accountProductType;
    private String accountStatus;
    private String linkStatus;
    private BigDecimal balance;
}