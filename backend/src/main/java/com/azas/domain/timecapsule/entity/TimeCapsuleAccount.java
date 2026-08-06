package com.azas.domain.timecapsule.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TimeCapsuleAccount {

    private Long financialAccountId;
    private Long childId;
    private String accountProductType;
    private String accountStatus;
    private String linkStatus;
    private LocalDate maturityDate;

    // [JMG] CAPSULE-1 자녀 명의의 활성 적금 계좌인지 판단한다.
    public boolean isEligibleSavingsAccount() {
        return childId != null
                && "SAVINGS".equals(accountProductType)
                && "ACTIVE".equals(accountStatus)
                && "ACTIVE".equals(linkStatus);
    }
}
