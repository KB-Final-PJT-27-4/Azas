package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.finance.account.entity.FinancialAccountUsagePolicy;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildAccountUsagePolicyServiceTest {

    private static final long PARENT_MEMBER_ID = 1L;
    private static final long CHILD_MEMBER_ID = 2L;
    private static final long OTHER_MEMBER_ID = 3L;
    private static final long ACCOUNT_ID = 10L;
    private static final long CHILD_ID = 20L;

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    private ChildAccountUsagePolicyService service;

    @BeforeEach
    void setUp() {
        service = new ChildAccountUsagePolicyService(
                financialAccountMapper
        );
    }

    @Test
    void parentCanReadUsagePolicy() {
        FinancialAccountUsagePolicy policy =
                eligiblePolicy(
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("50000.00")
                );

        when(financialAccountMapper
                .findUsagePolicyByAccountId(ACCOUNT_ID))
                .thenReturn(policy);
        when(financialAccountMapper
                .countActiveParentAccess(
                        PARENT_MEMBER_ID,
                        CHILD_ID
                ))
                .thenReturn(1);

        FinancialAccountUsagePolicy result =
                service.getUsagePolicy(
                        PARENT_MEMBER_ID,
                        ACCOUNT_ID
                );

        assertSame(policy, result);

        verify(
                financialAccountMapper,
                never()
        ).countActiveChildMemberAccess(
                PARENT_MEMBER_ID,
                CHILD_ID
        );
    }

    @Test
    void linkedChildCanReadUsagePolicy() {
        FinancialAccountUsagePolicy policy =
                eligiblePolicy(
                        ChildUsageMode.UNRESTRICTED,
                        null
                );

        when(financialAccountMapper
                .findUsagePolicyByAccountId(ACCOUNT_ID))
                .thenReturn(policy);
        when(financialAccountMapper
                .countActiveParentAccess(
                        CHILD_MEMBER_ID,
                        CHILD_ID
                ))
                .thenReturn(0);
        when(financialAccountMapper
                .countActiveChildMemberAccess(
                        CHILD_MEMBER_ID,
                        CHILD_ID
                ))
                .thenReturn(1);

        FinancialAccountUsagePolicy result =
                service.getUsagePolicy(
                        CHILD_MEMBER_ID,
                        ACCOUNT_ID
                );

        assertSame(policy, result);
    }

    @Test
    void rejectsUsagePolicyReadWithoutChildAccess() {
        FinancialAccountUsagePolicy policy =
                eligiblePolicy(
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("50000.00")
                );

        when(financialAccountMapper
                .findUsagePolicyByAccountId(ACCOUNT_ID))
                .thenReturn(policy);
        when(financialAccountMapper
                .countActiveParentAccess(
                        OTHER_MEMBER_ID,
                        CHILD_ID
                ))
                .thenReturn(0);
        when(financialAccountMapper
                .countActiveChildMemberAccess(
                        OTHER_MEMBER_ID,
                        CHILD_ID
                ))
                .thenReturn(0);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.getUsagePolicy(
                                OTHER_MEMBER_ID,
                                ACCOUNT_ID
                        )
                );

        assertEquals(
                ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsMissingAccount() {
        when(financialAccountMapper
                .findUsagePolicyByAccountId(ACCOUNT_ID))
                .thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.getUsagePolicy(
                                PARENT_MEMBER_ID,
                                ACCOUNT_ID
                        )
                );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsSavingsAccount() {
        FinancialAccountUsagePolicy policy =
                policy(
                        CHILD_ID,
                        "SAVINGS",
                        "ACTIVE",
                        "ACTIVE",
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("50000.00")
                );

        when(financialAccountMapper
                .findUsagePolicyByAccountId(ACCOUNT_ID))
                .thenReturn(policy);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.getUsagePolicy(
                                PARENT_MEMBER_ID,
                                ACCOUNT_ID
                        )
                );

        assertEquals(
                ErrorCode
                        .INELIGIBLE_CHILD_USAGE_POLICY_ACCOUNT,
                exception.getErrorCode()
        );
    }

    @Test
    void parentCanUpdateCoManagedPolicy() {
        FinancialAccountUsagePolicy currentPolicy =
                eligiblePolicy(
                        ChildUsageMode.UNRESTRICTED,
                        null
                );
        FinancialAccountUsagePolicy updatedPolicy =
                eligiblePolicy(
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("50000.00")
                );

        when(financialAccountMapper
                .findUsagePolicyByAccountIdForUpdate(
                        ACCOUNT_ID
                ))
                .thenReturn(currentPolicy);
        when(financialAccountMapper
                .countActiveParentAccess(
                        PARENT_MEMBER_ID,
                        CHILD_ID
                ))
                .thenReturn(1);
        when(financialAccountMapper.updateUsagePolicy(
                eq(ACCOUNT_ID),
                eq(ChildUsageMode.CO_MANAGED),
                eq(new BigDecimal("50000.00")),
                eq(PARENT_MEMBER_ID),
                any()
        )).thenReturn(1);
        when(financialAccountMapper
                .findUsagePolicyByAccountId(ACCOUNT_ID))
                .thenReturn(updatedPolicy);

        FinancialAccountUsagePolicy result =
                service.updateUsagePolicy(
                        PARENT_MEMBER_ID,
                        ACCOUNT_ID,
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("50000.00")
                );

        assertSame(updatedPolicy, result);
    }

    @Test
    void parentCanUpdateUnrestrictedPolicy() {
        FinancialAccountUsagePolicy currentPolicy =
                eligiblePolicy(
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("50000.00")
                );
        FinancialAccountUsagePolicy updatedPolicy =
                eligiblePolicy(
                        ChildUsageMode.UNRESTRICTED,
                        null
                );

        when(financialAccountMapper
                .findUsagePolicyByAccountIdForUpdate(
                        ACCOUNT_ID
                ))
                .thenReturn(currentPolicy);
        when(financialAccountMapper
                .countActiveParentAccess(
                        PARENT_MEMBER_ID,
                        CHILD_ID
                ))
                .thenReturn(1);
        when(financialAccountMapper.updateUsagePolicy(
                eq(ACCOUNT_ID),
                eq(ChildUsageMode.UNRESTRICTED),
                eq(null),
                eq(PARENT_MEMBER_ID),
                any()
        )).thenReturn(1);
        when(financialAccountMapper
                .findUsagePolicyByAccountId(ACCOUNT_ID))
                .thenReturn(updatedPolicy);

        FinancialAccountUsagePolicy result =
                service.updateUsagePolicy(
                        PARENT_MEMBER_ID,
                        ACCOUNT_ID,
                        ChildUsageMode.UNRESTRICTED,
                        null
                );

        assertSame(updatedPolicy, result);
    }

    @Test
    void rejectsCoManagedPolicyWithoutAmount() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.updateUsagePolicy(
                                PARENT_MEMBER_ID,
                                ACCOUNT_ID,
                                ChildUsageMode.CO_MANAGED,
                                null
                        )
                );

        assertEquals(
                ErrorCode.INVALID_CHILD_USAGE_POLICY,
                exception.getErrorCode()
        );

        verify(
                financialAccountMapper,
                never()
        ).findUsagePolicyByAccountIdForUpdate(
                ACCOUNT_ID
        );
    }

    @Test
    void rejectsNegativeMonthlyBudgetAmount() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.updateUsagePolicy(
                                PARENT_MEMBER_ID,
                                ACCOUNT_ID,
                                ChildUsageMode.CO_MANAGED,
                                new BigDecimal("-1.00")
                        )
                );

        assertEquals(
                ErrorCode.INVALID_CHILD_USAGE_POLICY,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsUnrestrictedPolicyWithAmount() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.updateUsagePolicy(
                                PARENT_MEMBER_ID,
                                ACCOUNT_ID,
                                ChildUsageMode.UNRESTRICTED,
                                new BigDecimal("50000.00")
                        )
                );

        assertEquals(
                ErrorCode.INVALID_CHILD_USAGE_POLICY,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsPolicyUpdateWithoutParentAccess() {
        FinancialAccountUsagePolicy policy =
                eligiblePolicy(
                        ChildUsageMode.UNRESTRICTED,
                        null
                );

        when(financialAccountMapper
                .findUsagePolicyByAccountIdForUpdate(
                        ACCOUNT_ID
                ))
                .thenReturn(policy);
        when(financialAccountMapper
                .countActiveParentAccess(
                        CHILD_MEMBER_ID,
                        CHILD_ID
                ))
                .thenReturn(0);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.updateUsagePolicy(
                                CHILD_MEMBER_ID,
                                ACCOUNT_ID,
                                ChildUsageMode.CO_MANAGED,
                                new BigDecimal("50000.00")
                        )
                );

        assertEquals(
                ErrorCode.PARENT_ACCESS_REQUIRED,
                exception.getErrorCode()
        );

        verify(
                financialAccountMapper,
                never()
        ).updateUsagePolicy(
                anyLong(),
                any(),
                any(),
                anyLong(),
                any()
        );
    }

    private FinancialAccountUsagePolicy eligiblePolicy(
            ChildUsageMode childUsageMode,
            BigDecimal amount
    ) {
        return policy(
                CHILD_ID,
                "DEMAND_DEPOSIT",
                "ACTIVE",
                "ACTIVE",
                childUsageMode,
                amount
        );
    }

    private FinancialAccountUsagePolicy policy(
            Long childId,
            String productType,
            String accountStatus,
            String linkStatus,
            ChildUsageMode childUsageMode,
            BigDecimal amount
    ) {
        FinancialAccountUsagePolicy policy =
                new FinancialAccountUsagePolicy();

        ReflectionTestUtils.setField(
                policy,
                "financialAccountId",
                ACCOUNT_ID
        );
        ReflectionTestUtils.setField(
                policy,
                "childId",
                childId
        );
        ReflectionTestUtils.setField(
                policy,
                "accountProductType",
                productType
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
                childUsageMode
        );
        ReflectionTestUtils.setField(
                policy,
                "childMonthlyBudgetAmount",
                amount
        );

        return policy;
    }
}