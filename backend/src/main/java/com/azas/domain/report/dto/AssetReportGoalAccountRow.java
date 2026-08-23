package com.azas.domain.report.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AssetReportGoalAccountRow {

    private Long financialGoalId;

    private String title;

    private BigDecimal targetAmount;

    private BigDecimal monthlySavingTargetAmount;

    private Long accountId;

    private String accountName;

    private String bankName;

    private byte[] accountNumberCiphertext;

    private BigDecimal balance;

    private BigDecimal monthlySavedAmount;
}
