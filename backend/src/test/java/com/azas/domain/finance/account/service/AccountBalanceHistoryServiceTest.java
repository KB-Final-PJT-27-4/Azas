package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountBalanceHistoryResult;
import com.azas.domain.finance.account.dto.AccountBalanceHistorySnapshotRow;
import com.azas.domain.finance.account.dto.AccountBalanceRow;
import com.azas.domain.finance.account.dto.MonthlyAccountBalanceResult;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountBalanceHistoryServiceTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 3L;
    private static final long CHILD_ID = 6L;
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-10T06:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    private AccountBalanceHistoryService service;

    @BeforeEach
    void setUp() {
        service = new AccountBalanceHistoryService(
                financialAccountMapper,
                FIXED_CLOCK
        );
    }

    @Test
    void returnsMonthlyLastBalancesAndChangesForParentAccount() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(parentAccount());
        when(financialAccountMapper.findBalanceSnapshotsByPeriod(
                ACCOUNT_ID,
                LocalDateTime.of(2026, 1, 31, 15, 0),
                LocalDateTime.of(2026, 8, 31, 15, 0)
        )).thenReturn(balanceSnapshots());

        AccountBalanceHistoryResult result = service.getBalanceHistory(
                MEMBER_ID,
                ACCOUNT_ID,
                6
        );

        assertEquals(ACCOUNT_ID, result.getAccountId());
        assertEquals(6, result.getMonths());
        assertEquals(YearMonth.of(2026, 3), result.getStartMonth());
        assertEquals(YearMonth.of(2026, 8), result.getEndMonth());
        assertEquals(6, result.getBalanceHistory().size());

        assertMonthlyBalance(
                result.getBalanceHistory().get(0),
                YearMonth.of(2026, 3),
                "800000.00",
                "100000.00"
        );
        assertMonthlyBalance(
                result.getBalanceHistory().get(1),
                YearMonth.of(2026, 4),
                "1000000.00",
                "200000.00"
        );

        MonthlyAccountBalanceResult may =
                result.getBalanceHistory().get(2);
        assertEquals(YearMonth.of(2026, 5), may.getMonth());
        assertNull(may.getBalance());
        assertNull(may.getChangeAmount());
        assertNull(may.getObservedAt());

        assertMonthlyBalance(
                result.getBalanceHistory().get(3),
                YearMonth.of(2026, 6),
                "1100000.00",
                null
        );
        assertMonthlyBalance(
                result.getBalanceHistory().get(4),
                YearMonth.of(2026, 7),
                "1200000.00",
                "100000.00"
        );
        assertMonthlyBalance(
                result.getBalanceHistory().get(5),
                YearMonth.of(2026, 8),
                "1250000.00",
                "50000.00"
        );
    }

    @Test
    void returnsRequestedEmptyMonthBucketsWhenNoSnapshotsExist() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(parentAccount());
        when(financialAccountMapper.findBalanceSnapshotsByPeriod(
                ACCOUNT_ID,
                LocalDateTime.of(2026, 4, 30, 15, 0),
                LocalDateTime.of(2026, 8, 31, 15, 0)
        )).thenReturn(List.of());

        AccountBalanceHistoryResult result = service.getBalanceHistory(
                MEMBER_ID,
                ACCOUNT_ID,
                3
        );

        assertEquals(YearMonth.of(2026, 6), result.getStartMonth());
        assertEquals(3, result.getBalanceHistory().size());

        for (MonthlyAccountBalanceResult month
                : result.getBalanceHistory()) {
            assertNull(month.getBalance());
            assertNull(month.getChangeAmount());
            assertNull(month.getObservedAt());
        }
    }

    @Test
    void keepsLatestObservedSnapshotWithinSameSeoulMonth() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(parentAccount());
        when(financialAccountMapper.findBalanceSnapshotsByPeriod(
                ACCOUNT_ID,
                LocalDateTime.of(2026, 6, 30, 15, 0),
                LocalDateTime.of(2026, 8, 31, 15, 0)
        )).thenReturn(List.of(
                snapshot("1000000.00", 2026, 7, 1, 1, 0),
                snapshot("1200000.00", 2026, 7, 31, 14, 0),
                snapshot("1100000.00", 2026, 7, 20, 2, 0),
                snapshot("1250000.00", 2026, 8, 10, 5, 0)
        ));

        AccountBalanceHistoryResult result = service.getBalanceHistory(
                MEMBER_ID,
                ACCOUNT_ID,
                1
        );

        MonthlyAccountBalanceResult august =
                result.getBalanceHistory().get(0);
        assertEquals(new BigDecimal("1250000.00"), august.getBalance());
        assertEquals(
                new BigDecimal("50000.00"),
                august.getChangeAmount()
        );
    }

    @Test
    void allowsAccessibleParentToReadChildAccountHistory() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(childAccount());
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        mockEmptySixMonthSnapshots();

        service.getBalanceHistory(MEMBER_ID, ACCOUNT_ID, 6);

        verify(financialAccountMapper, never())
                .countActiveChildMemberAccess(MEMBER_ID, CHILD_ID);
    }

    @Test
    void allowsLinkedChildMemberToReadOwnAccountHistory() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(childAccount());
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        mockEmptySixMonthSnapshots();

        service.getBalanceHistory(MEMBER_ID, ACCOUNT_ID, 6);

        verify(financialAccountMapper)
                .countActiveChildMemberAccess(MEMBER_ID, CHILD_ID);
    }

    @Test
    void rejectsMonthsOutsideOneToTwelve() {
        BusinessException zeroMonths = assertThrows(
                BusinessException.class,
                () -> service.getBalanceHistory(MEMBER_ID, ACCOUNT_ID, 0)
        );
        BusinessException thirteenMonths = assertThrows(
                BusinessException.class,
                () -> service.getBalanceHistory(MEMBER_ID, ACCOUNT_ID, 13)
        );

        assertEquals(
                ErrorCode.INVALID_BALANCE_HISTORY_MONTHS,
                zeroMonths.getErrorCode()
        );
        assertEquals(
                ErrorCode.INVALID_BALANCE_HISTORY_MONTHS,
                thirteenMonths.getErrorCode()
        );
        verify(financialAccountMapper, never())
                .findLinkedAccountBalanceById(ACCOUNT_ID);
    }

    @Test
    void rejectsInvalidAccountId() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getBalanceHistory(MEMBER_ID, 0L, 6)
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
    }

    @Test
    void returnsNotFoundForMissingOrUnavailableAccount() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getBalanceHistory(
                        MEMBER_ID,
                        ACCOUNT_ID,
                        6
                )
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsMemberWithoutParentAccountAccess() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(parentAccount());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getBalanceHistory(99L, ACCOUNT_ID, 6)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode()
        );
        verify(financialAccountMapper, never())
                .findBalanceSnapshotsByPeriod(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void rejectsMalformedSnapshotData() {
        AccountBalanceHistorySnapshotRow malformed =
                snapshot("1000.00", 2026, 8, 1, 0, 0);
        ReflectionTestUtils.setField(malformed, "balance", null);

        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(parentAccount());
        when(financialAccountMapper.findBalanceSnapshotsByPeriod(
                ACCOUNT_ID,
                LocalDateTime.of(2026, 1, 31, 15, 0),
                LocalDateTime.of(2026, 8, 31, 15, 0)
        )).thenReturn(List.of(malformed));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getBalanceHistory(
                        MEMBER_ID,
                        ACCOUNT_ID,
                        6
                )
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    private void mockEmptySixMonthSnapshots() {
        when(financialAccountMapper.findBalanceSnapshotsByPeriod(
                ACCOUNT_ID,
                LocalDateTime.of(2026, 1, 31, 15, 0),
                LocalDateTime.of(2026, 8, 31, 15, 0)
        )).thenReturn(List.of());
    }

    private List<AccountBalanceHistorySnapshotRow> balanceSnapshots() {
        return List.of(
                snapshot("700000.00", 2026, 2, 28, 14, 0),
                snapshot("750000.00", 2026, 3, 10, 5, 0),
                snapshot("800000.00", 2026, 3, 31, 14, 0),
                snapshot("1000000.00", 2026, 4, 30, 14, 0),
                snapshot("1100000.00", 2026, 6, 30, 14, 0),
                snapshot("1200000.00", 2026, 7, 31, 14, 0),
                snapshot("1250000.00", 2026, 8, 10, 5, 30)
        );
    }

    private void assertMonthlyBalance(
            MonthlyAccountBalanceResult actual,
            YearMonth expectedMonth,
            String expectedBalance,
            String expectedChange
    ) {
        assertEquals(expectedMonth, actual.getMonth());
        assertEquals(
                new BigDecimal(expectedBalance),
                actual.getBalance()
        );

        if (expectedChange == null) {
            assertNull(actual.getChangeAmount());
        } else {
            assertEquals(
                    new BigDecimal(expectedChange),
                    actual.getChangeAmount()
            );
        }
    }

    private AccountBalanceRow parentAccount() {
        AccountBalanceRow row = new AccountBalanceRow();
        ReflectionTestUtils.setField(row, "accountId", ACCOUNT_ID);
        ReflectionTestUtils.setField(row, "ownerType", "PARENT");
        ReflectionTestUtils.setField(
                row,
                "connectedByMemberId",
                MEMBER_ID
        );
        ReflectionTestUtils.setField(row, "balance", BigDecimal.ZERO);
        return row;
    }

    private AccountBalanceRow childAccount() {
        AccountBalanceRow row = new AccountBalanceRow();
        ReflectionTestUtils.setField(row, "accountId", ACCOUNT_ID);
        ReflectionTestUtils.setField(row, "ownerType", "CHILD");
        ReflectionTestUtils.setField(
                row,
                "connectedByMemberId",
                MEMBER_ID
        );
        ReflectionTestUtils.setField(row, "childId", CHILD_ID);
        ReflectionTestUtils.setField(row, "balance", BigDecimal.ZERO);
        return row;
    }

    private AccountBalanceHistorySnapshotRow snapshot(
            String balance,
            int year,
            int month,
            int day,
            int hour,
            int minute
    ) {
        AccountBalanceHistorySnapshotRow row =
                new AccountBalanceHistorySnapshotRow();
        ReflectionTestUtils.setField(
                row,
                "balance",
                new BigDecimal(balance)
        );
        ReflectionTestUtils.setField(
                row,
                "observedAt",
                LocalDateTime.of(year, month, day, hour, minute)
        );
        return row;
    }
}
