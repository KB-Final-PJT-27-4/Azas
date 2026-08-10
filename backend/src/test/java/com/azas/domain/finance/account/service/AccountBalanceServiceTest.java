package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountBalanceResult;
import com.azas.domain.finance.account.dto.AccountBalanceRow;
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
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 3L;
    private static final long CHILD_ID = 6L;

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    private AccountBalanceService service;

    @BeforeEach
    void setUp() {
        service = new AccountBalanceService(financialAccountMapper);
    }

    @Test
    void returnsParentAccountBalanceForConnectionOwner() {
        AccountBalanceRow row = parentAccountRow();

        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(row);

        AccountBalanceResult result = service.getLatestBalance(
                MEMBER_ID,
                ACCOUNT_ID
        );

        assertEquals(ACCOUNT_ID, result.getAccountId());
        assertEquals(
                new BigDecimal("1250000.00"),
                result.getBalance()
        );
        assertEquals(
                LocalDateTime.of(2026, 8, 10, 5, 30),
                result.getBalanceUpdatedAt()
        );

        verify(financialAccountMapper, never())
                .countActiveParentAccess(MEMBER_ID, CHILD_ID);
    }

    @Test
    void returnsChildAccountBalanceForAccessibleParent() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(childAccountRow());
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);

        AccountBalanceResult result = service.getLatestBalance(
                MEMBER_ID,
                ACCOUNT_ID
        );

        assertEquals(ACCOUNT_ID, result.getAccountId());
        verify(financialAccountMapper, never())
                .countActiveChildMemberAccess(MEMBER_ID, CHILD_ID);
    }

    @Test
    void returnsChildAccountBalanceForLinkedChildMember() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(childAccountRow());
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);

        AccountBalanceResult result = service.getLatestBalance(
                MEMBER_ID,
                ACCOUNT_ID
        );

        assertEquals(new BigDecimal("1250000.00"), result.getBalance());
    }

    @Test
    void acceptsZeroBalance() {
        AccountBalanceRow row = parentAccountRow();
        ReflectionTestUtils.setField(row, "balance", BigDecimal.ZERO);

        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(row);

        AccountBalanceResult result = service.getLatestBalance(
                MEMBER_ID,
                ACCOUNT_ID
        );

        assertEquals(BigDecimal.ZERO, result.getBalance());
    }

    @Test
    void rejectsInvalidAccountId() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getLatestBalance(MEMBER_ID, 0L)
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
        verify(financialAccountMapper, never())
                .findLinkedAccountBalanceById(0L);
    }

    @Test
    void returnsNotFoundForMissingOrUnavailableConnection() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getLatestBalance(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsDifferentMemberForParentAccount() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(parentAccountRow());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getLatestBalance(99L, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsMemberWithoutChildAccountAccess() {
        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(childAccountRow());
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getLatestBalance(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsInconsistentChildOwnershipData() {
        AccountBalanceRow row = childAccountRow();
        ReflectionTestUtils.setField(row, "childId", null);

        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(row);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getLatestBalance(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    @Test
    void returnsConflictWhenBalanceWasNeverSynchronized() {
        AccountBalanceRow row = parentAccountRow();
        ReflectionTestUtils.setField(row, "balanceUpdatedAt", null);

        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(row);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getLatestBalance(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.ACCOUNT_BALANCE_NOT_AVAILABLE,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsMissingBalanceAsInternalDataError() {
        AccountBalanceRow row = parentAccountRow();
        ReflectionTestUtils.setField(row, "balance", null);

        when(financialAccountMapper.findLinkedAccountBalanceById(
                ACCOUNT_ID
        )).thenReturn(row);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getLatestBalance(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    private AccountBalanceRow parentAccountRow() {
        AccountBalanceRow row = baseRow();
        ReflectionTestUtils.setField(row, "ownerType", "PARENT");
        ReflectionTestUtils.setField(
                row,
                "connectedByMemberId",
                MEMBER_ID
        );
        return row;
    }

    private AccountBalanceRow childAccountRow() {
        AccountBalanceRow row = baseRow();
        ReflectionTestUtils.setField(row, "ownerType", "CHILD");
        ReflectionTestUtils.setField(row, "childId", CHILD_ID);
        return row;
    }

    private AccountBalanceRow baseRow() {
        AccountBalanceRow row = new AccountBalanceRow();
        ReflectionTestUtils.setField(row, "accountId", ACCOUNT_ID);
        ReflectionTestUtils.setField(
                row,
                "balance",
                new BigDecimal("1250000.00")
        );
        ReflectionTestUtils.setField(
                row,
                "balanceUpdatedAt",
                LocalDateTime.of(2026, 8, 10, 5, 30)
        );
        return row;
    }
}
