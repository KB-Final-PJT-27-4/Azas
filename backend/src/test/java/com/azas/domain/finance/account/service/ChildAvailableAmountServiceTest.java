package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.ChildAvailableAmountAccountRow;
import com.azas.domain.finance.account.dto.ChildAvailableAmountResult;
import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.mapper.MemberMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildAvailableAmountServiceTest {

    private static final long MEMBER_ID = 9L;
    private static final long CHILD_ID = 6L;
    private static final long ACCOUNT_ID = 15L;
    private static final LocalDateTime MONTH_START =
            LocalDateTime.of(2026, 8, 1, 0, 0);
    private static final LocalDateTime NEXT_MONTH_START =
            LocalDateTime.of(2026, 9, 1, 0, 0);

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    @Mock
    private MemberMapper memberMapper;

    private ChildAvailableAmountService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-11T10:00:00Z"),
                ZoneOffset.UTC
        );

        service = new ChildAvailableAmountService(
                financialAccountMapper,
                memberMapper,
                clock
        );
    }

    @Test
    void returnsCoManagedCurrentMonthUsage() {
        mockChildMember();
        when(financialAccountMapper
                .findActivePrimaryChildDemandDepositByMemberId(
                        MEMBER_ID
                )).thenReturn(accountRow(
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("100000.00")
                ));
        when(financialAccountMapper
                .sumDebitAmountByAccountAndPeriod(
                        ACCOUNT_ID,
                        MONTH_START,
                        NEXT_MONTH_START
                )).thenReturn(new BigDecimal("35000.00"));

        ChildAvailableAmountResult result =
                service.getCurrentMonthUsage(MEMBER_ID);

        assertEquals(CHILD_ID, result.getChildId());
        assertEquals(ACCOUNT_ID, result.getAccountId());
        assertEquals(
                ChildUsageMode.CO_MANAGED,
                result.getChildUsageMode()
        );
        assertEquals(
                new BigDecimal("100000.00"),
                result.getChildMonthlyBudgetAmount()
        );
        assertEquals(
                new BigDecimal("35000.00"),
                result.getCurrentMonthSpentAmount()
        );
        assertEquals(
                new BigDecimal("65000.00"),
                result.getRemainingGuidanceAmount()
        );
        assertFalse(result.getBudgetExceeded());
        assertEquals("2026-08", result.getPeriod());
        assertEquals(
                LocalDateTime.of(2026, 8, 11, 10, 0),
                result.getCalculatedAt()
        );
    }

    @Test
    void returnsZeroRemainingAmountWhenBudgetWasExceeded() {
        mockChildMember();
        when(financialAccountMapper
                .findActivePrimaryChildDemandDepositByMemberId(
                        MEMBER_ID
                )).thenReturn(accountRow(
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("100000.00")
                ));
        when(financialAccountMapper
                .sumDebitAmountByAccountAndPeriod(
                        ACCOUNT_ID,
                        MONTH_START,
                        NEXT_MONTH_START
                )).thenReturn(new BigDecimal("120000.00"));

        ChildAvailableAmountResult result =
                service.getCurrentMonthUsage(MEMBER_ID);

        assertEquals(
                BigDecimal.ZERO,
                result.getRemainingGuidanceAmount()
        );
        assertTrue(result.getBudgetExceeded());
    }

    @Test
    void returnsUsageWithoutBudgetForUnrestrictedPolicy() {
        mockChildMember();
        when(financialAccountMapper
                .findActivePrimaryChildDemandDepositByMemberId(
                        MEMBER_ID
                )).thenReturn(accountRow(
                        ChildUsageMode.UNRESTRICTED,
                        null
                ));
        when(financialAccountMapper
                .sumDebitAmountByAccountAndPeriod(
                        ACCOUNT_ID,
                        MONTH_START,
                        NEXT_MONTH_START
                )).thenReturn(new BigDecimal("35000.00"));

        ChildAvailableAmountResult result =
                service.getCurrentMonthUsage(MEMBER_ID);

        assertNull(result.getChildMonthlyBudgetAmount());
        assertNull(result.getRemainingGuidanceAmount());
        assertNull(result.getBudgetExceeded());
        assertEquals(
                new BigDecimal("35000.00"),
                result.getCurrentMonthSpentAmount()
        );
    }

    @Test
    void rejectsParentMember() {
        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(Member.createParent(
                        "parent@example.com",
                        "부모",
                        null
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getCurrentMonthUsage(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.CHILD_MEMBER_ACCESS_REQUIRED,
                exception.getErrorCode()
        );
        verify(financialAccountMapper, never())
                .findActivePrimaryChildDemandDepositByMemberId(
                        MEMBER_ID
                );
    }

    @Test
    void returnsNotFoundWhenPrimaryDemandDepositIsUnavailable() {
        mockChildMember();
        when(financialAccountMapper
                .findActivePrimaryChildDemandDepositByMemberId(
                        MEMBER_ID
                )).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getCurrentMonthUsage(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsMissingUsagePolicy() {
        mockChildMember();
        when(financialAccountMapper
                .findActivePrimaryChildDemandDepositByMemberId(
                        MEMBER_ID
                )).thenReturn(accountRow(null, null));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getCurrentMonthUsage(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.CHILD_USAGE_POLICY_NOT_CONFIGURED,
                exception.getErrorCode()
        );
        verify(financialAccountMapper, never())
                .sumDebitAmountByAccountAndPeriod(
                        ACCOUNT_ID,
                        MONTH_START,
                        NEXT_MONTH_START
                );
    }

    @Test
    void rejectsMissingBudgetForCoManagedPolicy() {
        mockChildMember();
        when(financialAccountMapper
                .findActivePrimaryChildDemandDepositByMemberId(
                        MEMBER_ID
                )).thenReturn(accountRow(
                        ChildUsageMode.CO_MANAGED,
                        null
                ));
        when(financialAccountMapper
                .sumDebitAmountByAccountAndPeriod(
                        ACCOUNT_ID,
                        MONTH_START,
                        NEXT_MONTH_START
                )).thenReturn(BigDecimal.ZERO);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getCurrentMonthUsage(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    private void mockChildMember() {
        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(Member.createChild(
                        "child@example.com",
                        "자녀",
                        null
                ));
    }

    private ChildAvailableAmountAccountRow accountRow(
            ChildUsageMode usageMode,
            BigDecimal budgetAmount
    ) {
        ChildAvailableAmountAccountRow row =
                new ChildAvailableAmountAccountRow();
        ReflectionTestUtils.setField(row, "accountId", ACCOUNT_ID);
        ReflectionTestUtils.setField(row, "childId", CHILD_ID);
        ReflectionTestUtils.setField(
                row,
                "childUsageMode",
                usageMode
        );
        ReflectionTestUtils.setField(
                row,
                "childMonthlyBudgetAmount",
                budgetAmount
        );
        return row;
    }
}
