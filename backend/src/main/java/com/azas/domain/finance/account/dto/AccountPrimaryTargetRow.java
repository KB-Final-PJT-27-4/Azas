package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountPrimaryTargetRow {

    private Long accountId;
    private String ownerType;
    private Long ownerMemberId;
    private Long childId;
    private String accountStatus;
    private String linkStatus;
    private Boolean primaryAccount;
}
