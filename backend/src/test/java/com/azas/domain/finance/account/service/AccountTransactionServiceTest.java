package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.dto.AccountTransactionItemResult;
import com.azas.domain.finance.account.dto.AccountTransactionListResult;
import com.azas.domain.finance.account.dto.AccountTransactionRow;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
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
    private static final byte[] COUNTERPARTY_CIPHERTEXT =
            new byte[]{1, 2, 3};

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    @Mock
    private AccountDetailService accountDetailService;

    @Mock
    private AccountNumberProtector accountNumberProtector;

    private AccountTransactionService service;

    @BeforeEach
    void setUp() {
        service = new AccountTransactionService(
                financialAccountMapper,
                accountDetailService,
                accountNumberProtector
        );
    }

    @Test
    void returnsCreditAndDebitTransactionsForAccessibleAccount() {
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(financialAccountMapper.findAccountTransactions(
                ACCOUNT_ID, null, null, 21
        )).thenReturn(List.of(
                transaction(902L, "DEBIT", 11, 0),
                transaction(901L, "CREDIT", 10, 0)
        ));
        when(accountNumberProtector.decrypt(COUNTERPARTY_CIPHERTEXT))
                .thenReturn("123-456-789");

        AccountTransactionListResult result = service.getTransactions(
                MEMBER_ID, ACCOUNT_ID, null, null
        );

        assertEquals(ACCOUNT_ID, result.getAccountId());
        assertEquals(2, result.getTransactions().size());
        assertFalse(result.isHasNext());
        assertNull(result.getNextCursor());

        AccountTransactionItemResult debit =
                result.getTransactions().get(0);
        assertEquals(ACCOUNT_ID,
                debit.getWithdrawalAccount().getAccountId());
        assertEquals(7L, debit.getDepositAccount().getAccountId());
        assertEquals("상대 계좌",
                debit.getDepositAccount().getAccountName());

        AccountTransactionItemResult credit =
                result.getTransactions().get(1);
        assertEquals(ACCOUNT_ID, credit.getDepositAccount().getAccountId());
        assertEquals(7L, credit.getWithdrawalAccount().getAccountId());
        assertEquals("123-456-789",
                credit.getWithdrawalAccount().getAccountNumber());
    }

    @Test
    void usesStoredCounterpartyNameWhenAccountIsNotLinked() {
        AccountTransactionRow row = transaction(
                901L, "CREDIT", 10, 0
        );
        ReflectionTestUtils.setField(row, "counterpartyAccountId", null);
        ReflectionTestUtils.setField(row, "counterpartyBankName", null);
        ReflectionTestUtils.setField(row, "counterpartyAccountName", null);
        ReflectionTestUtils.setField(
                row,
                "counterpartyAccountNumberCiphertext",
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
                result.getTransactions().get(0)
                        .getWithdrawalAccount().getAccountName()
        );
        assertNull(result.getTransactions().get(0)
                .getWithdrawalAccount().getAccountId());
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
        when(accountNumberProtector.decrypt(COUNTERPARTY_CIPHERTEXT))
                .thenReturn("123-456-789");

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
    void returnsInternalErrorWhenCounterpartyAccountCannotBeDecrypted() {
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(financialAccountMapper.findAccountTransactions(
                ACCOUNT_ID, null, null, 21
        )).thenReturn(List.of(
                transaction(901L, "CREDIT", 10, 0)
        ));
        when(accountNumberProtector.decrypt(COUNTERPARTY_CIPHERTEXT))
                .thenThrow(new IllegalArgumentException());

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
                null,
                "004",
                "KB국민은행",
                "KB국민 5678",
                "987-6543-5678",
                "DEMAND_DEPOSIT",
                new BigDecimal("500000.00"),
                null,
                "ACTIVE",
                true,
                null,
                null,
                null,
                null
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
        ReflectionTestUtils.setField(row, "counterpartyAccountId", 7L);
        ReflectionTestUtils.setField(
                row,
                "counterpartyBankName",
                "KB국민은행"
        );
        ReflectionTestUtils.setField(
                row,
                "counterpartyAccountName",
                "상대 계좌"
        );
        ReflectionTestUtils.setField(
                row,
                "counterpartyAccountNumberCiphertext",
                COUNTERPARTY_CIPHERTEXT
        );
        return row;
    }
}
