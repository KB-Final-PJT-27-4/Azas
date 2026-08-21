package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.goal.dto.FinancialGoalCheckpointResult;
import com.azas.domain.finance.goal.dto.FinancialGoalCheckpointRow;
import com.azas.domain.finance.goal.dto.FinancialGoalDetailResult;
import com.azas.domain.finance.goal.dto.FinancialGoalDetailRow;
import com.azas.domain.finance.goal.dto.FinancialGoalListAccountResult;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialGoalDetailService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final MemberMapper memberMapper;
    private final FinancialGoalMapper financialGoalMapper;
    private final AccountNumberProtector accountNumberProtector;

    @Transactional(readOnly = true)
    public FinancialGoalDetailResult getGoal(
            long requesterMemberId,
            long financialGoalId
    ) {
        validateParent(requesterMemberId);
        if (financialGoalId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        List<FinancialGoalDetailRow> rows =
                financialGoalMapper.findAccessibleGoalDetailWithAccounts(
                        financialGoalId,
                        requesterMemberId
                );
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException(ErrorCode.FINANCIAL_GOAL_NOT_FOUND);
        }

        FinancialGoalDetailRow goal = rows.get(0);
        List<FinancialGoalListAccountResult> accounts = rows.stream()
                .map(this::toAccount)
                .toList();
        BigDecimal currentAmount = accounts.stream()
                .map(FinancialGoalListAccountResult::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal targetAmount = goal.getTargetAmount();

        List<FinancialGoalCheckpointRow> checkpointRows =
                financialGoalMapper.findGoalCheckpoints(financialGoalId);
        List<FinancialGoalCheckpointResult> checkpoints =
                checkpointRows == null
                        ? List.of()
                        : checkpointRows.stream()
                                .map(FinancialGoalDetailService::toCheckpoint)
                                .toList();

        return new FinancialGoalDetailResult(
                goal.getFinancialGoalId(),
                goal.getChildId(),
                goal.getFinancialGoalTemplateId(),
                goal.getTitle(),
                goal.getIconKey(),
                targetAmount,
                goal.getTargetDate(),
                goal.getMonthlySavingAmount(),
                currentAmount,
                remainingAmount(targetAmount, currentAmount),
                achievementRate(targetAmount, currentAmount),
                responseStatus(goal.getStatus(), targetAmount, currentAmount),
                accounts,
                checkpoints
        );
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

    private FinancialGoalListAccountResult toAccount(
            FinancialGoalDetailRow row
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

    private static FinancialGoalCheckpointResult toCheckpoint(
            FinancialGoalCheckpointRow row
    ) {
        return new FinancialGoalCheckpointResult(
                row.getFinancialGoalCheckpointId(),
                row.getPercentage(),
                row.getTargetAmount(),
                row.getReachedAt() != null,
                row.getReachedAt()
        );
    }

    private static BigDecimal zeroIfNull(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private static BigDecimal remainingAmount(
            BigDecimal targetAmount,
            BigDecimal currentAmount
    ) {
        return targetAmount.subtract(currentAmount).max(BigDecimal.ZERO);
    }

    private static BigDecimal achievementRate(
            BigDecimal targetAmount,
            BigDecimal currentAmount
    ) {
        if (targetAmount.signum() <= 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return currentAmount
                .multiply(ONE_HUNDRED)
                .divide(targetAmount, 1, RoundingMode.HALF_UP)
                .min(ONE_HUNDRED.setScale(1));
    }

    private static String responseStatus(
            String storedStatus,
            BigDecimal targetAmount,
            BigDecimal currentAmount
    ) {
        if ("ACHIEVED".equals(storedStatus)
                || currentAmount.compareTo(targetAmount) >= 0) {
            return "ACHIEVED";
        }
        return "ACTIVE";
    }
}
