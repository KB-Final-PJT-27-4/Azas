package com.azas.domain.finance.account.entity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialAccountUsagePolicyTest {

    @Test
    void coManagedRequiresMonthlyBudgetAmount() {
        assertTrue(
                ChildUsageMode.CO_MANAGED
                        .requiresMonthlyBudgetAmount()
        );
    }

    @Test
    void unrestrictedDoesNotRequireMonthlyBudgetAmount() {
        assertFalse(
                ChildUsageMode.UNRESTRICTED
                        .requiresMonthlyBudgetAmount()
        );
    }

    @Test
    void acceptsActiveLinkedChildDemandDepositAccount() {
        FinancialAccountUsagePolicy policy =
                createPolicy(
                        1L,
                        "DEMAND_DEPOSIT",
                        "ACTIVE",
                        "ACTIVE"
                );

        assertTrue(
                policy.isEligibleChildDemandDepositAccount()
        );
    }

    @Test
    void rejectsAccountWithoutChild() {
        FinancialAccountUsagePolicy policy =
                createPolicy(
                        null,
                        "DEMAND_DEPOSIT",
                        "ACTIVE",
                        "ACTIVE"
                );

        assertFalse(
                policy.isEligibleChildDemandDepositAccount()
        );
    }

    @Test
    void rejectsSavingsAccount() {
        FinancialAccountUsagePolicy policy =
                createPolicy(
                        1L,
                        "SAVINGS",
                        "ACTIVE",
                        "ACTIVE"
                );

        assertFalse(
                policy.isEligibleChildDemandDepositAccount()
        );
    }

    @Test
    void rejectsInactiveAccount() {
        FinancialAccountUsagePolicy policy =
                createPolicy(
                        1L,
                        "DEMAND_DEPOSIT",
                        "CLOSED",
                        "ACTIVE"
                );

        assertFalse(
                policy.isEligibleChildDemandDepositAccount()
        );
    }

    @Test
    void rejectsUnlinkedAccount() {
        FinancialAccountUsagePolicy policy =
                createPolicy(
                        1L,
                        "DEMAND_DEPOSIT",
                        "ACTIVE",
                        "UNLINKED"
                );

        assertFalse(
                policy.isEligibleChildDemandDepositAccount()
        );
    }

    private FinancialAccountUsagePolicy createPolicy(
            Long childId,
            String accountProductType,
            String accountStatus,
            String linkStatus
    ) {
        FinancialAccountUsagePolicy policy =
                new FinancialAccountUsagePolicy();

        ReflectionTestUtils.setField(
                policy,
                "financialAccountId",
                10L
        );
        ReflectionTestUtils.setField(
                policy,
                "childId",
                childId
        );
        ReflectionTestUtils.setField(
                policy,
                "accountProductType",
                accountProductType
        );
        ReflectionTestUtils.setField(
                policy,
                "accountStatus",
                accountStatus
        );
        ReflectionTestUtils.setField(
                policy,
                "linkStatus",
                linkStatus
        );
        ReflectionTestUtils.setField(
                policy,
                "childUsageMode",
                ChildUsageMode.CO_MANAGED
        );
        ReflectionTestUtils.setField(
                policy,
                "childMonthlyBudgetAmount",
                new BigDecimal("50000.00")
        );

        return policy;
    }
}