package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.finance.account.entity.FinancialAccountUsagePolicy;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ChildAccountUsagePolicyService {

    private static final int MAX_AMOUNT_PRECISION = 19;
    private static final int MAX_AMOUNT_SCALE = 2;

    private final FinancialAccountMapper financialAccountMapper;

    @Transactional(readOnly = true)
    public FinancialAccountUsagePolicy getUsagePolicy(
            long requesterMemberId,
            long financialAccountId
    ) {
        FinancialAccountUsagePolicy policy =
                findPolicyOrThrow(financialAccountId);

        validateEligibleAccount(policy);
        validateReadAccess(requesterMemberId, policy);

        return policy;
    }

    @Transactional
    public FinancialAccountUsagePolicy updateUsagePolicy(
            long requesterMemberId,
            long financialAccountId,
            ChildUsageMode childUsageMode,
            BigDecimal childMonthlyBudgetAmount
    ) {
        validatePolicyInput(
                childUsageMode,
                childMonthlyBudgetAmount
        );

        FinancialAccountUsagePolicy policy =
                financialAccountMapper
                        .findUsagePolicyByAccountIdForUpdate(
                                financialAccountId
                        );

        if (policy == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        validateEligibleAccount(policy);
        validateParentAccess(requesterMemberId, policy);

        LocalDateTime updatedAt =
                LocalDateTime.now(ZoneOffset.UTC);

        int updatedCount =
                financialAccountMapper.updateUsagePolicy(
                        financialAccountId,
                        childUsageMode,
                        childMonthlyBudgetAmount,
                        requesterMemberId,
                        updatedAt
                );

        if (updatedCount != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        FinancialAccountUsagePolicy updatedPolicy =
                financialAccountMapper
                        .findUsagePolicyByAccountId(
                                financialAccountId
                        );

        if (updatedPolicy == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        return updatedPolicy;
    }

    private FinancialAccountUsagePolicy findPolicyOrThrow(
            long financialAccountId
    ) {
        FinancialAccountUsagePolicy policy =
                financialAccountMapper
                        .findUsagePolicyByAccountId(
                                financialAccountId
                        );

        if (policy == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        return policy;
    }

    private void validateEligibleAccount(
            FinancialAccountUsagePolicy policy
    ) {
        if (!policy.isEligibleChildDemandDepositAccount()) {
            throw new BusinessException(
                    ErrorCode
                            .INELIGIBLE_CHILD_USAGE_POLICY_ACCOUNT
            );
        }
    }

    private void validateReadAccess(
            long requesterMemberId,
            FinancialAccountUsagePolicy policy
    ) {
        long childId = policy.getChildId();

        boolean hasParentAccess =
                financialAccountMapper
                        .countActiveParentAccess(
                                requesterMemberId,
                                childId
                        ) > 0;

        if (hasParentAccess) {
            return;
        }

        boolean hasChildAccess =
                financialAccountMapper
                        .countActiveChildMemberAccess(
                                requesterMemberId,
                                childId
                        ) > 0;

        if (!hasChildAccess) {
            throw new BusinessException(
                    ErrorCode.CHILD_ACCESS_DENIED
            );
        }
    }

    private void validateParentAccess(
            long requesterMemberId,
            FinancialAccountUsagePolicy policy
    ) {
        int parentAccessCount =
                financialAccountMapper
                        .countActiveParentAccess(
                                requesterMemberId,
                                policy.getChildId()
                        );

        if (parentAccessCount == 0) {
            throw new BusinessException(
                    ErrorCode.PARENT_ACCESS_REQUIRED
            );
        }
    }

    private void validatePolicyInput(
            ChildUsageMode childUsageMode,
            BigDecimal childMonthlyBudgetAmount
    ) {
        if (childUsageMode == null) {
            throw invalidPolicy();
        }

        if (childUsageMode.requiresMonthlyBudgetAmount()) {
            validateMonthlyBudgetAmount(
                    childMonthlyBudgetAmount
            );
            return;
        }

        if (childMonthlyBudgetAmount != null) {
            throw invalidPolicy();
        }
    }

    private void validateMonthlyBudgetAmount(
            BigDecimal amount
    ) {
        if (amount == null
                || amount.signum() < 0
                || amount.precision()
                > MAX_AMOUNT_PRECISION
                || amount.scale() > MAX_AMOUNT_SCALE) {
            throw invalidPolicy();
        }
    }

    private BusinessException invalidPolicy() {
        return new BusinessException(
                ErrorCode.INVALID_CHILD_USAGE_POLICY
        );
    }
}