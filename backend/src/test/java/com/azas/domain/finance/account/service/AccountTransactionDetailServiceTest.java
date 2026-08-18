package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.dto.AccountTransactionDetailResult;
import com.azas.domain.finance.account.dto.AccountTransactionDetailRow;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountTransactionDetailServiceTest {

    private static final long MEMBER_ID = 8L;
    private static final long TRANSACTION_ID = 901L;
    private static final long ACCOUNT_ID = 5L;
    private static final byte[] COUNTERPARTY_CIPHERTEXT =
            new byte[]{1, 2, 3};

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    @Mock
    private AccountDetailService accountDetailService;

    @Mock
    private AccountNumberProtector accountNumberProtector;

    private AccountTransactionDetailService service;

    @BeforeEach
    void setUp() {
        service = new AccountTransactionDetailService(
                financialAccountMapper,
                accountDetailService,
                accountNumberProtector
        );
    }

    @Test
    void returnsCreditTransactionDetailForAccessibleAccount() {
        AccountTransactionDetailRow row = transaction("CREDIT");
        when(financialAccountMapper.findAccountTransactionById(
                TRANSACTION_ID
        )).thenReturn(row);
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(accountNumberProtector.decrypt(COUNTERPARTY_CIPHERTEXT))
                .thenReturn("123-456-789");

        AccountTransactionDetailResult result = service
                .getTransactionDetail(MEMBER_ID, TRANSACTION_ID);

        assertEquals(TRANSACTION_ID, result.getAccountTransactionId());
        assertEquals("CREDIT", result.getDirection());
        assertEquals("첫 용돈", result.getMemo());
        assertEquals("아이사랑적금1",
                result.getDepositAccount().getAccountName());
        assertEquals("KB국민 5678",
                result.getWithdrawalAccount().getAccountName());
        assertEquals("123-456-789",
                result.getWithdrawalAccount().getAccountNumber());
        assertEquals(new BigDecimal("500000.00"),
                result.getBalanceAfter());
    }

    @Test
    void reversesDepositAndWithdrawalForDebitTransaction() {
        when(financialAccountMapper.findAccountTransactionById(
                TRANSACTION_ID
        )).thenReturn(transaction("DEBIT"));
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(accountNumberProtector.decrypt(COUNTERPARTY_CIPHERTEXT))
                .thenReturn("123-456-789");

        AccountTransactionDetailResult result = service
                .getTransactionDetail(MEMBER_ID, TRANSACTION_ID);

        assertEquals("KB국민 5678",
                result.getDepositAccount().getAccountName());
        assertEquals("아이사랑적금1",
                result.getWithdrawalAccount().getAccountName());
    }

    @Test
    void supportsUnlinkedExternalCounterparty() {
        AccountTransactionDetailRow row = transaction("CREDIT");
        ReflectionTestUtils.setField(row, "counterpartyBankName", null);
        ReflectionTestUtils.setField(row, "counterpartyAccountName", null);
        ReflectionTestUtils.setField(
                row,
                "counterpartyAccountNumberCiphertext",
                null
        );
        when(financialAccountMapper.findAccountTransactionById(
                TRANSACTION_ID
        )).thenReturn(row);
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());

        AccountTransactionDetailResult result = service
                .getTransactionDetail(MEMBER_ID, TRANSACTION_ID);

        assertEquals("외부 상대방",
                result.getWithdrawalAccount().getAccountName());
        assertNull(result.getWithdrawalAccount().getBankName());
        assertNull(result.getWithdrawalAccount().getAccountNumber());
    }

    @Test
    void rejectsInvalidTransactionIdBeforeLookup() {
        assertError(
                ErrorCode.BADREQUEST,
                () -> service.getTransactionDetail(MEMBER_ID, 0)
        );
        verify(financialAccountMapper, never())
                .findAccountTransactionById(0);
    }

    @Test
    void returnsNotFoundWhenTransactionDoesNotExist() {
        when(financialAccountMapper.findAccountTransactionById(
                TRANSACTION_ID
        )).thenReturn(null);

        assertError(
                ErrorCode.ACCOUNT_TRANSACTION_NOT_FOUND,
                () -> service.getTransactionDetail(
                        MEMBER_ID,
                        TRANSACTION_ID
                )
        );
    }

    @Test
    void mapsUnavailableLedgerAccountToTransactionNotFound() {
        when(financialAccountMapper.findAccountTransactionById(
                TRANSACTION_ID
        )).thenReturn(transaction("CREDIT"));
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenThrow(new BusinessException(
                        ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
                ));

        assertError(
                ErrorCode.ACCOUNT_TRANSACTION_NOT_FOUND,
                () -> service.getTransactionDetail(
                        MEMBER_ID,
                        TRANSACTION_ID
                )
        );
    }

    @Test
    void preservesAccountAccessDeniedError() {
        when(financialAccountMapper.findAccountTransactionById(
                TRANSACTION_ID
        )).thenReturn(transaction("CREDIT"));
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenThrow(new BusinessException(
                        ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED
                ));

        assertError(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                () -> service.getTransactionDetail(
                        MEMBER_ID,
                        TRANSACTION_ID
                )
        );
    }

    @Test
    void returnsInternalErrorWhenCounterpartyNumberCannotBeDecrypted() {
        when(financialAccountMapper.findAccountTransactionById(
                TRANSACTION_ID
        )).thenReturn(transaction("CREDIT"));
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(currentAccount());
        when(accountNumberProtector.decrypt(COUNTERPARTY_CIPHERTEXT))
                .thenThrow(new IllegalArgumentException());

        assertError(
                ErrorCode.INTERNAL_SERVER_ERROR,
                () -> service.getTransactionDetail(
                        MEMBER_ID,
                        TRANSACTION_ID
                )
        );
    }

    @Test
    void returnsInternalErrorForMalformedStoredTransaction() {
        AccountTransactionDetailRow row = transaction("INVALID");
        when(financialAccountMapper.findAccountTransactionById(
                TRANSACTION_ID
        )).thenReturn(row);

        assertError(
                ErrorCode.INTERNAL_SERVER_ERROR,
                () -> service.getTransactionDetail(
                        MEMBER_ID,
                        TRANSACTION_ID
                )
        );
        verify(accountDetailService, never())
                .getAccountDetail(MEMBER_ID, ACCOUNT_ID);
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
                "아이사랑적금1",
                "952-17362605-43",
                "깨비",
                "SAVINGS",
                new BigDecimal("500000.00")
        );
    }

    private AccountTransactionDetailRow transaction(String direction) {
        AccountTransactionDetailRow row =
                new AccountTransactionDetailRow();
        ReflectionTestUtils.setField(
                row,
                "accountTransactionId",
                TRANSACTION_ID
        );
        ReflectionTestUtils.setField(
                row,
                "financialAccountId",
                ACCOUNT_ID
        );
        ReflectionTestUtils.setField(
                row,
                "occurredAt",
                LocalDateTime.of(2026, 7, 23, 6, 0)
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
                "counterpartyBankName",
                "KB국민은행"
        );
        ReflectionTestUtils.setField(
                row,
                "counterpartyAccountName",
                "KB국민 5678"
        );
        ReflectionTestUtils.setField(
                row,
                "counterpartyAccountNumberCiphertext",
                COUNTERPARTY_CIPHERTEXT
        );
        return row;
    }
}
