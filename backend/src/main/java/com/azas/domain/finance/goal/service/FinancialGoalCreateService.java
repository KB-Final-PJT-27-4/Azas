package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.finance.goal.dto.*;
import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import com.azas.domain.finance.goal.mapper.FinancialGoalMapper;
import com.azas.domain.finance.goal.mapper.FinancialGoalTemplateMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FinancialGoalCreateService {
    private static final List<Integer> CHECKPOINTS = List.of(10, 25, 50, 75, 100);

    private final MemberMapper memberMapper;
    private final FinancialAccountMapper accountMapper;
    private final FinancialGoalTemplateMapper templateMapper;
    private final FinancialGoalMapper goalMapper;
    private final Clock clock;

    @Autowired
    public FinancialGoalCreateService(MemberMapper memberMapper,
                                      FinancialAccountMapper accountMapper,
                                      FinancialGoalTemplateMapper templateMapper,
                                      FinancialGoalMapper goalMapper) {
        this(memberMapper, accountMapper, templateMapper, goalMapper, Clock.systemUTC());
    }

    FinancialGoalCreateService(MemberMapper memberMapper,
                               FinancialAccountMapper accountMapper,
                               FinancialGoalTemplateMapper templateMapper,
                               FinancialGoalMapper goalMapper,
                               Clock clock) {
        this.memberMapper = memberMapper;
        this.accountMapper = accountMapper;
        this.templateMapper = templateMapper;
        this.goalMapper = goalMapper;
        this.clock = clock;
    }

    @Transactional
    public FinancialGoalCreateResult create(long requesterMemberId, long childId,
                                             FinancialGoalCreateCommand command) {
        validateParent(requesterMemberId);
        validateChildAccess(requesterMemberId, childId);
        ValidatedRequest request = validateRequest(command);
        String title = resolveTitle(request);
        List<FinancialGoalAccountTargetRow> accounts =
                findAndValidateAccounts(requesterMemberId, childId, request.accountIds());

        BigDecimal currentAmount = accounts.stream()
                .map(FinancialGoalAccountTargetRow::getBalance)
                .map(value -> value == null ? BigDecimal.ZERO : value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (currentAmount.compareTo(request.targetAmount()) >= 0) {
            throw new BusinessException(ErrorCode.FINANCIAL_GOAL_ALREADY_REACHED);
        }

        Instant createdAt = clock.instant();
        LocalDateTime createdAtUtc = LocalDateTime.ofInstant(createdAt, ZoneOffset.UTC);
        BigDecimal remainingAmount = request.targetAmount().subtract(currentAmount);
        long remainingMonths = ChronoUnit.MONTHS.between(
                YearMonth.from(createdAtUtc), YearMonth.from(request.targetDate()));
        BigDecimal monthlySavingAmount = remainingAmount.divide(
                BigDecimal.valueOf(remainingMonths), 0, RoundingMode.CEILING);

        FinancialGoalInsertCommand insert = new FinancialGoalInsertCommand(
                childId, request.templateId(), title, request.targetAmount(),
                request.targetDate(), monthlySavingAmount, createdAtUtc);
        if (goalMapper.insertFinancialGoal(insert) != 1
                || insert.getFinancialGoalId() == null) {
            throw internalError();
        }

        long goalId = insert.getFinancialGoalId();
        for (FinancialGoalAccountTargetRow account : accounts) {
            if (goalMapper.insertFinancialGoalAccount(goalId, account.getAccountId()) != 1) {
                throw internalError();
            }
        }
        insertCheckpoints(goalId, request.targetAmount(), currentAmount, createdAtUtc);

        BigDecimal rate = currentAmount.multiply(BigDecimal.valueOf(100))
                .divide(request.targetAmount(), 1, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100));
        return new FinancialGoalCreateResult(
                goalId, childId, request.templateId(), title,
                request.targetAmount(), request.targetDate(), monthlySavingAmount,
                currentAmount, remainingAmount, rate, "ACTIVE",
                accounts.stream().map(this::toLinkedAccount).toList(), createdAt);
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
        if (childId < 1) throw invalidRequest();
        if (accountMapper.countActiveChildById(childId) < 1) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
        if (accountMapper.countActiveParentAccess(memberId, childId) < 1) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }
    }

    private ValidatedRequest validateRequest(FinancialGoalCreateCommand command) {
        if (command == null) throw invalidRequest();
        Long templateId = command.getFinancialGoalTemplateId();
        String title = command.getTitle();
        if (templateId != null) {
            if (templateId < 1 || title != null) throw invalidRequest();
        } else {
            title = normalizeTitle(title);
            if (title == null || title.length() > 100) throw invalidRequest();
        }
        BigDecimal targetAmount = command.getTargetAmount();
        if (targetAmount == null || targetAmount.signum() <= 0) throw invalidRequest();
        LocalDate targetDate = command.getTargetDate();
        YearMonth now = YearMonth.from(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        if (targetDate == null || !YearMonth.from(targetDate).isAfter(now)) {
            throw invalidRequest();
        }
        List<Long> accountIds = command.getAccountIds();
        if (accountIds == null || accountIds.isEmpty()
                || accountIds.stream().anyMatch(id -> id == null || id < 1)) {
            throw invalidRequest();
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(accountIds);
        if (uniqueIds.size() != accountIds.size()) throw invalidRequest();
        return new ValidatedRequest(templateId, title, targetAmount,
                targetDate, List.copyOf(uniqueIds));
    }

    private String resolveTitle(ValidatedRequest request) {
        if (request.templateId() == null) return request.title();
        FinancialGoalTemplate template = templateMapper.findActiveDefaultTemplateById(
                request.templateId());
        if (template == null) {
            throw new BusinessException(ErrorCode.FINANCIAL_GOAL_TEMPLATE_NOT_FOUND);
        }
        return template.getGoalName();
    }

    private List<FinancialGoalAccountTargetRow> findAndValidateAccounts(
            long requesterMemberId, long childId, List<Long> accountIds) {
        List<FinancialGoalAccountTargetRow> rows =
                goalMapper.findAccountTargetsForUpdate(accountIds);
        if (rows == null || rows.size() != accountIds.size()) {
            throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND);
        }
        Map<Long, FinancialGoalAccountTargetRow> byId = rows.stream()
                .collect(Collectors.toMap(FinancialGoalAccountTargetRow::getAccountId,
                        Function.identity()));
        List<FinancialGoalAccountTargetRow> ordered = accountIds.stream()
                .map(byId::get).toList();
        if (ordered.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND);
        }
        for (FinancialGoalAccountTargetRow row : ordered) {
            if (!isEligibleGoalAccount(row, requesterMemberId, childId)
                    || !"SAVINGS".equals(row.getAccountProductType())
                    || !"ACTIVE".equals(row.getAccountStatus())
                    || !"ACTIVE".equals(row.getLinkStatus())) {
                throw new BusinessException(ErrorCode.INELIGIBLE_FINANCIAL_GOAL_ACCOUNT);
            }
            if (row.getFinancialGoalId() != null) {
                throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_GOAL_ALREADY_ASSIGNED);
            }
        }
        return ordered;
    }

    private boolean isEligibleGoalAccount(
            FinancialGoalAccountTargetRow account,
            long requesterMemberId,
            long childId
    ) {
        if ("CHILD".equals(account.getOwnerType())) {
            return Objects.equals(account.getChildId(), childId);
        }
        if ("PARENT".equals(account.getOwnerType())) {
            return Objects.equals(account.getOwnerMemberId(), requesterMemberId);
        }
        return false;
    }

    private void insertCheckpoints(long goalId, BigDecimal targetAmount,
                                   BigDecimal currentAmount, LocalDateTime createdAt) {
        for (int percentage : CHECKPOINTS) {
            BigDecimal amount = targetAmount.multiply(BigDecimal.valueOf(percentage))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            LocalDateTime reachedAt = currentAmount.compareTo(amount) >= 0
                    ? createdAt : null;
            if (goalMapper.insertFinancialGoalCheckpoint(
                    new FinancialGoalCheckpointInsertCommand(
                            goalId, percentage, amount, reachedAt, createdAt)) != 1) {
                throw internalError();
            }
        }
    }

    private FinancialGoalLinkedAccountResult toLinkedAccount(
            FinancialGoalAccountTargetRow account) {
        return new FinancialGoalLinkedAccountResult(
                account.getAccountId(), account.getAccountName(), account.getBankName(),
                account.getBalance() == null ? BigDecimal.ZERO : account.getBalance());
    }

    private String normalizeTitle(String title) {
        if (title == null) return null;
        String normalized = title.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private BusinessException invalidRequest() {
        return new BusinessException(ErrorCode.INVALID_FINANCIAL_GOAL_REQUEST);
    }

    private BusinessException internalError() {
        return new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private record ValidatedRequest(Long templateId, String title,
                                    BigDecimal targetAmount, LocalDate targetDate,
                                    List<Long> accountIds) {
    }
}
