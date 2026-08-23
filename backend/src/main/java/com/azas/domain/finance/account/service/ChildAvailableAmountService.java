package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.ChildAvailableAmountAccountRow;
import com.azas.domain.finance.account.dto.ChildAvailableAmountResult;
import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.child.service.ChildFeaturePermissionService;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class ChildAvailableAmountService {

    private final FinancialAccountMapper financialAccountMapper;
    private final MemberMapper memberMapper;
    private final ChildFeaturePermissionService childFeaturePermissionService;
    private final Clock clock;

    @Autowired
    public ChildAvailableAmountService(
            FinancialAccountMapper financialAccountMapper,
            MemberMapper memberMapper,
            ChildFeaturePermissionService childFeaturePermissionService
    ) {
        this(
                financialAccountMapper,
                memberMapper,
                childFeaturePermissionService,
                Clock.systemUTC()
        );
    }

    ChildAvailableAmountService(
            FinancialAccountMapper financialAccountMapper,
            MemberMapper memberMapper,
            ChildFeaturePermissionService childFeaturePermissionService,
            Clock clock
    ) {
        this.financialAccountMapper = financialAccountMapper;
        this.memberMapper = memberMapper;
        this.childFeaturePermissionService = childFeaturePermissionService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ChildAvailableAmountResult getCurrentMonthUsage(
            long requesterMemberId
    ) {
        validateChildMember(requesterMemberId);

        Long childId = financialAccountMapper
                .findActiveChildIdByMemberId(requesterMemberId);

        if (childId == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        childFeaturePermissionService
                .validateUsageLimitViewEnabled(childId);

        ChildAvailableAmountAccountRow account =
                financialAccountMapper
                        .findActivePrimaryChildDemandDepositByMemberId(
                                requesterMemberId
                        );

        if (account == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        if (!childId.equals(account.getChildId())) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        ChildUsageMode usageMode = account.getChildUsageMode();

        if (usageMode == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_USAGE_POLICY_NOT_CONFIGURED
            );
        }

        LocalDateTime calculatedAt = LocalDateTime.now(clock);
        YearMonth period = YearMonth.from(calculatedAt);
        LocalDateTime startOccurredAt =
                period.atDay(1).atStartOfDay();
        LocalDateTime endOccurredAtExclusive =
                period.plusMonths(1).atDay(1).atStartOfDay();

        BigDecimal spentAmount = financialAccountMapper
                .sumDebitAmountByAccountAndPeriod(
                        account.getAccountId(),
                        startOccurredAt,
                        endOccurredAtExclusive
                );

        validateSpentAmount(spentAmount);

        BigDecimal budgetAmount =
                account.getChildMonthlyBudgetAmount();
        BigDecimal remainingGuidanceAmount = null;
        Boolean budgetExceeded = null;

        if (usageMode.requiresMonthlyBudgetAmount()) {
            validateBudgetAmount(budgetAmount);

            BigDecimal difference = budgetAmount.subtract(
                    spentAmount
            );
            remainingGuidanceAmount = difference.max(
                    BigDecimal.ZERO
            );
            budgetExceeded = spentAmount.compareTo(
                    budgetAmount
            ) > 0;
        } else {
            budgetAmount = null;
        }

        return new ChildAvailableAmountResult(
                account.getChildId(),
                account.getAccountId(),
                usageMode,
                budgetAmount,
                spentAmount,
                remainingGuidanceAmount,
                budgetExceeded,
                period.toString(),
                calculatedAt
        );
    }

    private void validateChildMember(long requesterMemberId) {
        Member member = memberMapper.findById(requesterMemberId);

        if (member == null
                || member.getMemberType() != MemberType.CHILD) {
            throw new BusinessException(
                    ErrorCode.CHILD_MEMBER_ACCESS_REQUIRED
            );
        }
    }

    private void validateSpentAmount(BigDecimal spentAmount) {
        if (spentAmount == null || spentAmount.signum() < 0) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    private void validateBudgetAmount(BigDecimal budgetAmount) {
        if (budgetAmount == null || budgetAmount.signum() < 0) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }
}
