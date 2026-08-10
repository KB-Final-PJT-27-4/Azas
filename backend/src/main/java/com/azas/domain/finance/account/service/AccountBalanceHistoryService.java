package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountBalanceHistoryResult;
import com.azas.domain.finance.account.dto.AccountBalanceHistorySnapshotRow;
import com.azas.domain.finance.account.dto.AccountBalanceRow;
import com.azas.domain.finance.account.dto.MonthlyAccountBalanceResult;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountBalanceHistoryService {

    private static final int MIN_MONTHS = 1;
    private static final int MAX_MONTHS = 12;
    private static final String PARENT_OWNER_TYPE = "PARENT";
    private static final String CHILD_OWNER_TYPE = "CHILD";
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final FinancialAccountMapper financialAccountMapper;
    private final Clock clock;

    @Autowired
    public AccountBalanceHistoryService(
            FinancialAccountMapper financialAccountMapper
    ) {
        this(financialAccountMapper, Clock.systemUTC());
    }

    AccountBalanceHistoryService(
            FinancialAccountMapper financialAccountMapper,
            Clock clock
    ) {
        this.financialAccountMapper = financialAccountMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AccountBalanceHistoryResult getBalanceHistory(
            long requesterMemberId,
            long financialAccountId,
            int months
    ) {
        validateFinancialAccountId(financialAccountId);
        validateMonths(months);

        AccountBalanceRow account = financialAccountMapper
                .findLinkedAccountBalanceById(financialAccountId);

        if (account == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        validateAccess(requesterMemberId, account);

        YearMonth endMonth = YearMonth.now(clock.withZone(SERVICE_ZONE));
        YearMonth startMonth = endMonth.minusMonths(months - 1L);
        YearMonth baselineMonth = startMonth.minusMonths(1L);

        List<AccountBalanceHistorySnapshotRow> snapshots =
                financialAccountMapper.findBalanceSnapshotsByPeriod(
                        financialAccountId,
                        toUtcBoundary(baselineMonth),
                        toUtcBoundary(endMonth.plusMonths(1L))
                );

        Map<YearMonth, AccountBalanceHistorySnapshotRow> lastByMonth =
                groupLastSnapshotByMonth(snapshots);

        return new AccountBalanceHistoryResult(
                financialAccountId,
                months,
                startMonth,
                endMonth,
                createMonthlyHistory(startMonth, months, lastByMonth)
        );
    }

    private void validateFinancialAccountId(long financialAccountId) {
        if (financialAccountId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void validateMonths(int months) {
        if (months < MIN_MONTHS || months > MAX_MONTHS) {
            throw new BusinessException(
                    ErrorCode.INVALID_BALANCE_HISTORY_MONTHS
            );
        }
    }

    private void validateAccess(
            long requesterMemberId,
            AccountBalanceRow account
    ) {
        if (PARENT_OWNER_TYPE.equals(account.getOwnerType())) {
            if (account.getConnectedByMemberId() != null
                    && account.getConnectedByMemberId()
                    == requesterMemberId) {
                return;
            }

            throw accessDenied();
        }

        if (CHILD_OWNER_TYPE.equals(account.getOwnerType())) {
            validateChildAccountAccess(requesterMemberId, account);
            return;
        }

        throw accessDenied();
    }

    private void validateChildAccountAccess(
            long requesterMemberId,
            AccountBalanceRow account
    ) {
        Long childId = account.getChildId();

        if (childId == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        boolean parentAccess = financialAccountMapper
                .countActiveParentAccess(requesterMemberId, childId) > 0;

        if (parentAccess) {
            return;
        }

        boolean childAccess = financialAccountMapper
                .countActiveChildMemberAccess(
                        requesterMemberId,
                        childId
                ) > 0;

        if (!childAccess) {
            throw accessDenied();
        }
    }

    private LocalDateTime toUtcBoundary(YearMonth month) {
        Instant instant = month.atDay(1)
                .atStartOfDay(SERVICE_ZONE)
                .toInstant();

        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private Map<YearMonth, AccountBalanceHistorySnapshotRow>
    groupLastSnapshotByMonth(
            List<AccountBalanceHistorySnapshotRow> snapshots
    ) {
        Map<YearMonth, AccountBalanceHistorySnapshotRow> lastByMonth =
                new HashMap<>();

        for (AccountBalanceHistorySnapshotRow snapshot : snapshots) {
            validateSnapshot(snapshot);

            YearMonth month = toServiceMonth(snapshot.getObservedAt());
            AccountBalanceHistorySnapshotRow previous =
                    lastByMonth.get(month);

            if (previous == null
                    || snapshot.getObservedAt().isAfter(
                    previous.getObservedAt()
            )) {
                lastByMonth.put(month, snapshot);
            }
        }

        return lastByMonth;
    }

    private void validateSnapshot(
            AccountBalanceHistorySnapshotRow snapshot
    ) {
        if (snapshot == null
                || snapshot.getBalance() == null
                || snapshot.getObservedAt() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    private YearMonth toServiceMonth(LocalDateTime observedAt) {
        return YearMonth.from(
                observedAt.toInstant(ZoneOffset.UTC)
                        .atZone(SERVICE_ZONE)
        );
    }

    private List<MonthlyAccountBalanceResult> createMonthlyHistory(
            YearMonth startMonth,
            int months,
            Map<YearMonth, AccountBalanceHistorySnapshotRow> lastByMonth
    ) {
        List<MonthlyAccountBalanceResult> history =
                new ArrayList<>(months);

        for (int index = 0; index < months; index++) {
            YearMonth month = startMonth.plusMonths(index);
            AccountBalanceHistorySnapshotRow current =
                    lastByMonth.get(month);
            AccountBalanceHistorySnapshotRow previous =
                    lastByMonth.get(month.minusMonths(1L));

            history.add(toMonthlyResult(month, current, previous));
        }

        return List.copyOf(history);
    }

    private MonthlyAccountBalanceResult toMonthlyResult(
            YearMonth month,
            AccountBalanceHistorySnapshotRow current,
            AccountBalanceHistorySnapshotRow previous
    ) {
        if (current == null) {
            return new MonthlyAccountBalanceResult(
                    month,
                    null,
                    null,
                    null
            );
        }

        BigDecimal changeAmount = previous == null
                ? null
                : current.getBalance().subtract(previous.getBalance());

        return new MonthlyAccountBalanceResult(
                month,
                current.getBalance(),
                changeAmount,
                current.getObservedAt()
        );
    }

    private BusinessException accessDenied() {
        return new BusinessException(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED
        );
    }
}
