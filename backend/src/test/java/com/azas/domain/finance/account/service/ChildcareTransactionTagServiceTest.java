package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.dto.AccountTransactionDetailRow;
import com.azas.domain.finance.account.dto.ChildcareTransactionTagResponse;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildcareTransactionTagServiceTest {

    private static final long MEMBER_ID = 8L;
    private static final long TRANSACTION_ID = 901L;
    private static final long ACCOUNT_ID = 40001L;
    private static final long CHILD_ID = 6L;

    @Mock
    private FinancialAccountMapper financialAccountMapper;
    @Mock
    private AccountDetailService accountDetailService;

    private ChildcareTransactionTagService service;

    @BeforeEach
    void setUp() {
        service = new ChildcareTransactionTagService(
                financialAccountMapper,
                accountDetailService
        );
    }

    @Test
    void tagsExternalDebitForAccessibleChild() {
        when(financialAccountMapper.findAccountTransactionById(TRANSACTION_ID))
                .thenReturn(externalDebit());
        when(financialAccountMapper.countActiveChildById(CHILD_ID)).thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(MEMBER_ID, CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.updateChildcareChildIdForExternalDebit(
                TRANSACTION_ID, CHILD_ID
        )).thenReturn(1);

        ChildcareTransactionTagResponse response = service.updateTag(
                MEMBER_ID, TRANSACTION_ID, CHILD_ID
        );

        assertTrue(response.isChildcareIncluded());
        assertEquals(CHILD_ID, response.getChildcareChildId());
        verify(accountDetailService).getAccountDetail(MEMBER_ID, ACCOUNT_ID);
        verify(financialAccountMapper).updateChildcareChildIdForExternalDebit(
                TRANSACTION_ID, CHILD_ID
        );
    }

    @Test
    void removesTagWithoutRequiringChildId() {
        when(financialAccountMapper.findAccountTransactionById(TRANSACTION_ID))
                .thenReturn(externalDebit());
        when(financialAccountMapper.updateChildcareChildIdForExternalDebit(
                TRANSACTION_ID, null
        )).thenReturn(1);

        ChildcareTransactionTagResponse response = service.updateTag(
                MEMBER_ID, TRANSACTION_ID, null
        );

        assertFalse(response.isChildcareIncluded());
        assertNull(response.getChildcareChildId());
        verify(financialAccountMapper, never()).countActiveChildById(CHILD_ID);
    }

    @Test
    void rejectsInternalTransferBeforeUpdatingTag() {
        AccountTransactionDetailRow transaction = externalDebit();
        ReflectionTestUtils.setField(transaction, "counterpartyAccountId", 40002L);
        when(financialAccountMapper.findAccountTransactionById(TRANSACTION_ID))
                .thenReturn(transaction);

        assertError(ErrorCode.INELIGIBLE_CHILDCARE_TRANSACTION,
                () -> service.updateTag(MEMBER_ID, TRANSACTION_ID, CHILD_ID));

        verify(financialAccountMapper, never())
                .updateChildcareChildIdForExternalDebit(TRANSACTION_ID, CHILD_ID);
    }

    @Test
    void rejectsChildWithoutRequesterParentAccess() {
        when(financialAccountMapper.findAccountTransactionById(TRANSACTION_ID))
                .thenReturn(externalDebit());
        when(financialAccountMapper.countActiveChildById(CHILD_ID)).thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(MEMBER_ID, CHILD_ID))
                .thenReturn(0);

        assertError(ErrorCode.PARENT_ACCESS_REQUIRED,
                () -> service.updateTag(MEMBER_ID, TRANSACTION_ID, CHILD_ID));
    }

    private AccountTransactionDetailRow externalDebit() {
        AccountTransactionDetailRow row = new AccountTransactionDetailRow();
        ReflectionTestUtils.setField(row, "accountTransactionId", TRANSACTION_ID);
        ReflectionTestUtils.setField(row, "financialAccountId", ACCOUNT_ID);
        ReflectionTestUtils.setField(row, "direction", "DEBIT");
        ReflectionTestUtils.setField(row, "ownerType", "PARENT");
        return row;
    }

    private void assertError(ErrorCode expected,
                             org.junit.jupiter.api.function.Executable executable) {
        BusinessException exception = assertThrows(BusinessException.class, executable);
        assertEquals(expected, exception.getErrorCode());
    }
}
