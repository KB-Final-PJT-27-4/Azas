package com.azas.domain.finance.account.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class FinancialAccountUsagePolicy {

    private Long financialAccountId;
    private Long childId;
    private String accountProductType;
    private String accountStatus;
    private String linkStatus;
    private ChildUsageMode childUsageMode;
    private BigDecimal childMonthlyBudgetAmount;
    private Long usagePolicyUpdatedByMemberId;
    private LocalDateTime usagePolicyUpdatedAt;

    // 금융기관 사용을 차단하지 않고 관리 기준을 설정할 수 있는 계좌인지 판단한다.
    public boolean isEligibleChildDemandDepositAccount() {
        return childId != null
                && "DEMAND_DEPOSIT".equals(accountProductType)
                && "ACTIVE".equals(accountStatus)
                && "ACTIVE".equals(linkStatus);
    }
}