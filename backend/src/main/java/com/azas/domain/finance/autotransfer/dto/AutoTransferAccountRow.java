package com.azas.domain.finance.autotransfer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AutoTransferAccountRow {

    private Long financialAccountId;
    private String ownerType;
    private Long ownerMemberId;
    private Long childId;
    private String accountProductType;
    private String accountStatus;
    private String linkStatus;
    private Long financialGoalId;
}