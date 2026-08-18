package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.dto.AccountTransactionItemResult;
import com.azas.domain.finance.account.dto.AccountTransactionListResult;
import com.azas.domain.finance.account.dto.AccountTransactionRow;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountTransactionServiceTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 3L;

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    @Mock
    private AccountDetailService accountDetailService;

    private AccountTransactionService service;

    @BeforeEach
    void setUp() {
        service = new AccountTransactionService(
                financialAccountMapper,
                accountDetailService
        );
    }

    @Test
    void returnsMinimalTransactionItemsForAccessibleAccount() {
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(financialAccountMapper.findAccountTransactions(
                ACCOUNT_ID, null, null, 21
        )).thenReturn(List.of(
                transaction(902L, "DEBIT", 11, 0),
                transaction(901L, "CREDIT", 10, 0)
        ));

        AccountTransactionListResult result = service.getTransactions(
                MEMBER_ID, ACCOUNT_ID, null, null
        );

        assertEquals(ACCOUNT_ID, result.getAccountId());
        assertEquals(2, result.getTransactions().size());
        assertFalse(result.isHasNext());
        assertNull(result.getNextCursor());

        AccountTransactionItemResult debit =
                result.getTransactions().get(0);
        assertEquals(902L, debit.getAccountTransactionId());
        assertEquals("상대 계좌", debit.getCounterpartyName());
        assertEquals("DEBIT", debit.getDirection());
        assertEquals(new BigDecimal("100000.00"), debit.getAmount());
    }

    @Test
    void usesStoredCounterpartyNameWhenAccountIsNotLinked() {
        AccountTransactionRow row = transaction(
                901L, "CREDIT", 10, 0
        );
        ReflectionTestUtils.setField(
                row,
                "counterpartyAccountName",
                null
        );

        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(financialAccountMapper.findAccountTransactions(
                ACCOUNT_ID, null, null, 21
        )).thenReturn(List.of(row));

        AccountTransactionListResult result = service.getTransactions(
                MEMBER_ID, ACCOUNT_ID, null, 20
        );

        assertEquals(
                "외부 상대방",
                result.getTransactions().get(0).getCounterpartyName()
        );
    }

    @Test
    void returnsNullCounterpartyNameWhenItCannotBeResolved() {
        AccountTransactionRow row = transaction(
                901L, "CREDIT", 10, 0
        );
        ReflectionTestUtils.setField(row, "counterpartyAccountName", null);
        ReflectionTestUtils.setField(row, "counterpartyName", null);

        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(financialAccountMapper.findAccountTransactions(
                ACCOUNT_ID, null, null, 21
        )).thenReturn(List.of(row));

        AccountTransactionListResult result = service.getTransactions(
                MEMBER_ID, ACCOUNT_ID, null, 20
        );

        assertNull(result.getTransactions().get(0).getCounterpartyName());
    }

    @Test
    void returnsOpaqueCursorAndUsesItForNextPage() {
        LocalDateTime cursorTime = LocalDateTime.of(
                2026, 7, 21, 2, 2
        );
        AccountTransactionRow first = transaction(
                902L, "CREDIT", 21, 2
        );
        AccountTransactionRow extra = transaction(
                901L, "CREDIT", 20, 1
        );

        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(financialAccountMapper.findAccountTransactions(
                ACCOUNT_ID, null, null, 2
        )).thenReturn(List.of(first, extra));
        when(financialAccountMapper.findAccountTransactions(
                ACCOUNT_ID, cursorTime, 902L, 2
        )).thenReturn(List.of(extra));

        AccountTransactionListResult firstPage = service.getTransactions(
                MEMBER_ID, ACCOUNT_ID, null, 1
        );

        assertTrue(firstPage.isHasNext());
        assertNotNull(firstPage.getNextCursor());
        assertEquals(1, firstPage.getTransactions().size());

        AccountTransactionListResult secondPage = service.getTransactions(
                MEMBER_ID,
                ACCOUNT_ID,
                firstPage.getNextCursor(),
                1
        );

        assertFalse(secondPage.isHasNext());
        assertEquals(901L, secondPage.getTransactions().get(0)
                .getAccountTransactionId());
        verify(financialAccountMapper).findAccountTransactions(
                ACCOUNT_ID, cursorTime, 902L, 2
        );
    }

    @Test
    void returnsEmptyListWhenAccountHasNoTransactions() {
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(financialAccountMapper.findAccountTransactions(
                ACCOUNT_ID, null, null, 21
        )).thenReturn(List.of());

        AccountTransactionListResult result = service.getTransactions(
                MEMBER_ID, ACCOUNT_ID, null, 20
        );

        assertTrue(result.getTransactions().isEmpty());
        assertFalse(result.isHasNext());
        assertNull(result.getNextCursor());
    }

    @Test
    void rejectsInvalidSizeAndCursorBeforeAccountLookup() {
        assertError(
                ErrorCode.INVALID_QUERY_PARAMETER,
                () -> service.getTransactions(
                        MEMBER_ID, ACCOUNT_ID, null, 0
                )
        );
        assertError(
                ErrorCode.INVALID_QUERY_PARAMETER,
                () -> service.getTransactions(
                        MEMBER_ID, ACCOUNT_ID, "invalid", 20
                )
        );

        verify(accountDetailService, never())
                .getAccountDetail(MEMBER_ID, ACCOUNT_ID);
    }

    @Test
    void returnsInternalErrorForMalformedStoredTransaction() {
        AccountTransactionRow row = transaction(
                901L, "INVALID", 10, 0
        );
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(financialAccountMapper.findAccountTransactions(
                ACCOUNT_ID, null, null, 21
        )).thenReturn(List.of(row));

        assertError(
                ErrorCode.INTERNAL_SERVER_ERROR,
                () -> service.getTransactions(
                        MEMBER_ID, ACCOUNT_ID, null, 20
                )
        );
    }

    private void assertError(
            ErrorCode expected,
            org.junit.jupiter.api.function.Executable executable
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                executable
        );
        assertEquals(expected, exception.getErrorCode());
    }

    private AccountDetailResult currentAccount() {
        return new AccountDetailResult(
                ACCOUNT_ID,
                "CHILD",
                "KB국민은행",
                "KB국민 5678",
                "987-6543-5678",
                "깨비",
                "DEMAND_DEPOSIT",
                new BigDecimal("500000.00")
        );
    }

    private AccountTransactionRow transaction(
            long transactionId,
            String direction,
            int day,
            int minute
    ) {
        AccountTransactionRow row = new AccountTransactionRow();
        ReflectionTestUtils.setField(
                row,
                "accountTransactionId",
                transactionId
        );
        ReflectionTestUtils.setField(
                row,
                "occurredAt",
                LocalDateTime.of(2026, 7, day, 2, minute)
        );
        ReflectionTestUtils.setField(row, "direction", direction);
        ReflectionTestUtils.setField(
                row,
                "amount",
                new BigDecimal("100000.00")
        );
        ReflectionTestUtils.setField(
                row,
                "balanceAfter",
                new BigDecimal("500000.00")
        );
        ReflectionTestUtils.setField(row, "description", "첫 용돈");
        ReflectionTestUtils.setField(
                row,
                "counterpartyName",
                "외부 상대방"
        );
        ReflectionTestUtils.setField(
                row,
                "counterpartyAccountName",
                "상대 계좌"
        );
        return row;
    }
}
