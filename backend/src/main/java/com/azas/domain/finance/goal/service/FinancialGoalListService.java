package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.finance.goal.dto.FinancialGoalListAccountResult;
import com.azas.domain.finance.goal.dto.FinancialGoalListItemResult;
import com.azas.domain.finance.goal.dto.FinancialGoalListResult;
import com.azas.domain.finance.goal.dto.FinancialGoalListRow;
import com.azas.domain.finance.goal.mapper.FinancialGoalMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinancialGoalListService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final MemberMapper memberMapper;
    private final FinancialAccountMapper financialAccountMapper;
    private final FinancialGoalMapper financialGoalMapper;
    private final AccountNumberProtector accountNumberProtector;

    @Transactional(readOnly = true)
    public FinancialGoalListResult getGoals(
            long requesterMemberId,
            long childId
    ) {
        validateParent(requesterMemberId);
        validateChildAccess(requesterMemberId, childId);

        List<FinancialGoalListRow> rows =
                financialGoalMapper.findActiveAndAchievedGoalsWithAccounts(
                        childId
                );

        if (rows == null || rows.isEmpty()) {
            return new FinancialGoalListResult(childId, List.of());
        }

        Map<Long, GoalAccumulator> goals = new LinkedHashMap<>();
        for (FinancialGoalListRow row : rows) {
            GoalAccumulator goal = goals.computeIfAbsent(
                    row.getFinancialGoalId(),
                    ignored -> new GoalAccumulator(row)
            );
            goal.addAccount(toAccount(row));
        }

        List<FinancialGoalListItemResult> results = goals.values().stream()
                .map(GoalAccumulator::toResult)
                .toList();
        return new FinancialGoalListResult(childId, results);
    }

    private void validateParent(long memberId) {
        Member member = memberMapper.findById(memberId);
        if (member == null || member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        if (member.getMemberType() != MemberType.PARENT) {
            throw new BusinessException(ErrorCode.PARENT_ACCESS_REQUIRED);
        }
    }

    private void validateChildAccess(long memberId, long childId) {
        if (childId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
        if (financialAccountMapper.countActiveChildById(childId) < 1) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
        if (financialAccountMapper.countActiveParentAccess(memberId, childId) < 1) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }
    }

    private FinancialGoalListAccountResult toAccount(
            FinancialGoalListRow row
    ) {
        return new FinancialGoalListAccountResult(
                row.getAccountId(),
                row.getAccountName(),
                row.getBankName(),
                decryptAccountNumber(row.getAccountNumberCiphertext()),
                zeroIfNull(row.getBalance())
        );
    }

    private String decryptAccountNumber(byte[] ciphertext) {
        try {
            return accountNumberProtector.decrypt(ciphertext);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private static BigDecimal zeroIfNull(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private static final class GoalAccumulator {

        private final FinancialGoalListRow goal;
        private final List<FinancialGoalListAccountResult> accounts =
                new ArrayList<>();
        private BigDecimal currentAmount = BigDecimal.ZERO;

        private GoalAccumulator(FinancialGoalListRow goal) {
            this.goal = goal;
        }

        private void addAccount(FinancialGoalListAccountResult account) {
            accounts.add(account);
            currentAmount = currentAmount.add(account.getBalance());
        }

        private FinancialGoalListItemResult toResult() {
            BigDecimal targetAmount = goal.getTargetAmount();
            BigDecimal remainingAmount = targetAmount
                    .subtract(currentAmount)
                    .max(BigDecimal.ZERO);
            BigDecimal achievementRate = currentAmount
                    .multiply(ONE_HUNDRED)
                    .divide(targetAmount, 1, RoundingMode.HALF_UP)
                    .min(ONE_HUNDRED)
                    .setScale(1, RoundingMode.HALF_UP);
            String status = currentAmount.compareTo(targetAmount) >= 0
                    ? "ACHIEVED"
                    : goal.getStatus();

            return new FinancialGoalListItemResult(
                    goal.getFinancialGoalId(),
                    goal.getFinancialGoalTemplateId(),
                    goal.getTitle(),
                    goal.getIconKey(),
                    targetAmount,
                    currentAmount,
                    remainingAmount,
                    achievementRate,
                    goal.getTargetDate(),
                    status,
                    List.copyOf(accounts)
            );
        }
    }
}
