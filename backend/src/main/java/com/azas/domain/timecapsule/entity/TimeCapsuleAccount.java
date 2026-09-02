package com.azas.domain.timecapsule.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TimeCapsuleAccount {

    private Long financialAccountId;
    private String ownerType;
    private Long ownerMemberId;
    private Long childId;
    private String accountName;
    private String accountProductType;
    private LocalDate maturityDate;
    private String accountStatus;
    private String linkStatus;

    public boolean isEligibleTimeCapsuleAccount() {
        return ("DEMAND_DEPOSIT".equals(accountProductType)
                || "SAVINGS".equals(accountProductType))
                && "ACTIVE".equals(accountStatus)
                && "ACTIVE".equals(linkStatus);
    }

    public boolean isParentOwnedBy(long memberId) {
        return "PARENT".equals(ownerType)
                && ownerMemberId != null
                && ownerMemberId == memberId;
    }

    public boolean isOwnedByChild(long targetChildId) {
        return "CHILD".equals(ownerType)
                && childId != null
                && childId == targetChildId;
    }
}
