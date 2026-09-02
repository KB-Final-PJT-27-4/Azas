package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.goal.dto.FinancialGoalAccountTargetRow;
import com.azas.domain.finance.goal.dto.FinancialGoalCheckpointRow;
import com.azas.domain.finance.goal.dto.FinancialGoalDetailResult;
import com.azas.domain.finance.goal.dto.FinancialGoalUpdateRequest;
import com.azas.domain.finance.goal.dto.FinancialGoalUpdateTargetRow;
import com.azas.domain.finance.goal.mapper.FinancialGoalMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FinancialGoalUpdateService {

    private final MemberMapper memberMapper;
    private final FinancialGoalMapper goalMapper;
    private final FinancialGoalDetailService detailService;
    private final Clock clock;

    @Autowired
    public FinancialGoalUpdateService(
            MemberMapper memberMapper,
            FinancialGoalMapper goalMapper,
            FinancialGoalDetailService detailService
    ) {
        this(memberMapper, goalMapper, detailService, Clock.systemUTC());
    }

    FinancialGoalUpdateService(
            MemberMapper memberMapper,
            FinancialGoalMapper goalMapper,
            FinancialGoalDetailService detailService,
            Clock clock
    ) {
        this.memberMapper = memberMapper;
        this.goalMapper = goalMapper;
        this.detailService = detailService;
        this.clock = clock;
    }

    @Transactional
    public FinancialGoalDetailResult update(
            long requesterMemberId,
            long financialGoalId,
            FinancialGoalUpdateRequest request
    ) {
        validateParent(requesterMemberId);
        if (financialGoalId < 1 || request == null || noChangeRequested(request)) {
            throw invalidRequest();
        }

        FinancialGoalUpdateTargetRow goal = goalMapper.findAccessibleGoalForUpdate(
                financialGoalId, requesterMemberId);
        if (goal == null) {
            throw new BusinessException(ErrorCode.FINANCIAL_GOAL_NOT_FOUND);
        }

        BigDecimal targetAmount = request.getTargetAmount() == null
                ? goal.getTargetAmount() : request.getTargetAmount();
        LocalDate targetDate = request.getTargetDate() == null
                ? goal.getTargetDate() : request.getTargetDate();
        validateGoalSettings(targetAmount, targetDate);

        List<Long> existingIds = safeList(goalMapper.findGoalAccountIds(financialGoalId));
        List<Long> finalIds = request.getAccountIds() == null
                ? existingIds : validateAccountIds(request.getAccountIds());
        if (finalIds.isEmpty()) {
            throw invalidRequest();
        }

        Set<Long> allIds = new LinkedHashSet<>(existingIds);
        allIds.addAll(finalIds);
        List<FinancialGoalAccountTargetRow> lockedAccounts =
                goalMapper.findAccountTargetsForUpdate(new ArrayList<>(allIds));
        Map<Long, FinancialGoalAccountTargetRow> byId = validateAndIndexAccounts(
                goal, requesterMemberId, financialGoalId, allIds, lockedAccounts);

        List<FinancialGoalAccountTargetRow> finalAccounts = finalIds.stream()
                .map(byId::get)
                .toList();
        BigDecimal currentAmount = finalAccounts.stream()
                .map(FinancialGoalAccountTargetRow::getBalance)
                .map(FinancialGoalUpdateService::zeroIfNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (currentAmount.compareTo(targetAmount) >= 0) {
            throw new BusinessException(ErrorCode.FINANCIAL_GOAL_ALREADY_REACHED);
        }

        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        long remainingMonths = ChronoUnit.MONTHS.between(
                YearMonth.from(now), YearMonth.from(targetDate));
        BigDecimal monthlySavingAmount = targetAmount.subtract(currentAmount)
                .divide(BigDecimal.valueOf(remainingMonths), 0, RoundingMode.CEILING);

        applyAccountLinks(financialGoalId, existingIds, finalIds);
        if (goalMapper.updateFinancialGoal(financialGoalId, targetAmount, targetDate,
                monthlySavingAmount, "ACTIVE") != 1) {
            throw internalError();
        }
        recalculateCheckpoints(financialGoalId, targetAmount, currentAmount, now);

        return detailService.getGoal(requesterMemberId, financialGoalId);
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

    private boolean noChangeRequested(FinancialGoalUpdateRequest request) {
        return request.getTargetAmount() == null
                && request.getTargetDate() == null
                && request.getAccountIds() == null;
    }

    private void validateGoalSettings(BigDecimal targetAmount, LocalDate targetDate) {
        YearMonth now = YearMonth.from(LocalDateTime.ofInstant(
                clock.instant(), ZoneOffset.UTC));
        if (targetAmount == null || targetAmount.signum() <= 0
                || targetDate == null
                || !YearMonth.from(targetDate).isAfter(now)) {
            throw invalidRequest();
        }
    }

    private List<Long> validateAccountIds(List<Long> accountIds) {
        if (accountIds.isEmpty()
                || accountIds.stream().anyMatch(id -> id == null || id < 1)) {
            throw invalidRequest();
        }
        Set<Long> unique = new LinkedHashSet<>(accountIds);
        if (unique.size() != accountIds.size()) {
            throw invalidRequest();
        }
        return List.copyOf(unique);
    }

    private Map<Long, FinancialGoalAccountTargetRow> validateAndIndexAccounts(
            FinancialGoalUpdateTargetRow goal,
            long requesterMemberId,
            long financialGoalId,
            Set<Long> requestedIds,
            List<FinancialGoalAccountTargetRow> accounts
    ) {
        if (accounts == null || accounts.size() != requestedIds.size()) {
            throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND);
        }
        Map<Long, FinancialGoalAccountTargetRow> byId = accounts.stream()
                .collect(Collectors.toMap(
                        FinancialGoalAccountTargetRow::getAccountId,
                        Function.identity()));
        for (Long accountId : requestedIds) {
            FinancialGoalAccountTargetRow account = byId.get(accountId);
            if (account == null) {
                throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND);
            }
            if (!isEligibleGoalAccount(account, requesterMemberId, goal.getChildId())
                    || !"SAVINGS".equals(account.getAccountProductType())
                    || !"ACTIVE".equals(account.getAccountStatus())
                    || !"ACTIVE".equals(account.getLinkStatus())) {
                throw new BusinessException(ErrorCode.INELIGIBLE_FINANCIAL_GOAL_ACCOUNT);
            }
            if (account.getFinancialGoalId() != null
                    && account.getFinancialGoalId() != financialGoalId) {
                throw new BusinessException(
                        ErrorCode.FINANCIAL_ACCOUNT_GOAL_ALREADY_ASSIGNED);
            }
        }
        return byId;
    }

    private boolean isEligibleGoalAccount(
            FinancialGoalAccountTargetRow account,
            long requesterMemberId,
            Long childId
    ) {
        if ("CHILD".equals(account.getOwnerType())) {
            return Objects.equals(account.getChildId(), childId);
        }
        if ("PARENT".equals(account.getOwnerType())) {
            return Objects.equals(account.getOwnerMemberId(), requesterMemberId);
        }
        return false;
    }

    private void applyAccountLinks(
            long financialGoalId,
            List<Long> existingIds,
            List<Long> finalIds
    ) {
        Set<Long> existing = new LinkedHashSet<>(existingIds);
        Set<Long> target = new LinkedHashSet<>(finalIds);
        for (Long accountId : existing) {
            if (!target.contains(accountId)) {
                if (goalMapper.deleteFinancialGoalAccount(
                        financialGoalId, accountId) != 1) {
                    throw internalError();
                }
            }
        }
        for (Long accountId : target) {
            if (!existing.contains(accountId)) {
                try {
                    if (goalMapper.insertFinancialGoalAccount(
                            financialGoalId, accountId) != 1) {
                        throw internalError();
                    }
                } catch (DuplicateKeyException exception) {
                    throw new BusinessException(
                            ErrorCode.FINANCIAL_ACCOUNT_GOAL_ALREADY_ASSIGNED);
                }
            }
        }
    }

    private void recalculateCheckpoints(
            long financialGoalId,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            LocalDateTime now
    ) {
        List<FinancialGoalCheckpointRow> checkpoints =
                goalMapper.findGoalCheckpoints(financialGoalId);
        if (checkpoints == null || checkpoints.size() != 5) {
            throw internalError();
        }
        for (FinancialGoalCheckpointRow checkpoint : checkpoints) {
            BigDecimal checkpointAmount = targetAmount
                    .multiply(BigDecimal.valueOf(checkpoint.getPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            LocalDateTime reachedAt = currentAmount.compareTo(checkpointAmount) >= 0
                    ? (checkpoint.getReachedAt() == null
                    ? now : LocalDateTime.ofInstant(
                    checkpoint.getReachedAt(), ZoneOffset.UTC))
                    : null;
            if (goalMapper.updateFinancialGoalCheckpoint(
                    checkpoint.getFinancialGoalCheckpointId(),
                    checkpointAmount, reachedAt) != 1) {
                throw internalError();
            }
        }
    }

    private static List<Long> safeList(List<Long> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BusinessException invalidRequest() {
        return new BusinessException(ErrorCode.INVALID_FINANCIAL_GOAL_REQUEST);
    }

    private BusinessException internalError() {
        return new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
