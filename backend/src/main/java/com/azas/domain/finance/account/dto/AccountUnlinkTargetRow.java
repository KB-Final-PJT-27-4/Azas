package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AccountUnlinkTargetRow {

    private Long accountId;
    private String ownerType;
    private Long ownerMemberId;
    private Long childId;
    private String linkStatus;
    private LocalDateTime unlinkedAt;
}
