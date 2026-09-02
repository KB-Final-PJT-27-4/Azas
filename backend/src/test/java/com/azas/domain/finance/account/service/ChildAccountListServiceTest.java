package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.ChildAccountListResult;
import com.azas.domain.finance.account.dto.ChildAccountListRow;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildAccountListServiceTest {

    private static final long MEMBER_ID = 8L;
    private static final long CHILD_ID = 6L;

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    @Mock
    private AccountNumberProtector accountNumberProtector;

    private ChildAccountListService service;

    @BeforeEach
    void setUp() {
        service = new ChildAccountListService(
                financialAccountMapper,
                accountNumberProtector
        );
    }

    @Test
    void accessibleParentReadsChildAccountsAndTotalBalance() {
        byte[] firstCiphertext = {1, 2, 3};
        byte[] secondCiphertext = {4, 5, 6};
        allowParentAccess();
        when(financialAccountMapper.findActiveChildAccounts(CHILD_ID))
                .thenReturn(List.of(
                        accountRow(2L, firstCiphertext, "9600000.00"),
                        accountRow(3L, secondCiphertext, "5000000.00")
                ));
        when(accountNumberProtector.decrypt(firstCiphertext))
                .thenReturn("952-17362605-43");
        when(accountNumberProtector.decrypt(secondCiphertext))
                .thenReturn("123-456-789012");

        ChildAccountListResult result = service.getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        );

        assertEquals(CHILD_ID, result.getChildId());
        assertEquals(2, result.getAccounts().size());
        assertEquals("952-17362605-43",
                result.getAccounts().get(0).getAccountNumber());
        assertEquals(true, result.getAccounts().get(0).isPrimary());
        assertEquals(new BigDecimal("14600000.00"),
                result.getTotalBalance());
    }

    @Test
    void linkedChildMemberCanReadOwnEmptyAccountList() {
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID, CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID, CHILD_ID
        )).thenReturn(1);
        when(financialAccountMapper.findActiveChildAccounts(CHILD_ID))
                .thenReturn(List.of());

        ChildAccountListResult result = service.getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        );

        assertEquals(BigDecimal.ZERO, result.getTotalBalance());
        assertEquals(0, result.getAccounts().size());
    }

    @Test
    void nullRowsBecomeZeroTotalAndEmptyList() {
        allowParentAccess();
        when(financialAccountMapper.findActiveChildAccounts(CHILD_ID))
                .thenReturn(null);

        ChildAccountListResult result = service.getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        );

        assertEquals(BigDecimal.ZERO, result.getTotalBalance());
        assertEquals(0, result.getAccounts().size());
    }

    @Test
    void rejectsMissingOrDeletedChild() {
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getChildAccounts(MEMBER_ID, CHILD_ID)
        );

        assertEquals(ErrorCode.CHILD_NOT_FOUND, exception.getErrorCode());
        verify(financialAccountMapper, never())
                .findActiveChildAccounts(CHILD_ID);
    }

    @Test
    void rejectsInvalidChildId() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getChildAccounts(MEMBER_ID, 0L)
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
        verify(financialAccountMapper, never()).countActiveChildById(0L);
    }

    @Test
    void rejectsMemberWithoutChildAccess() {
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID, CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID, CHILD_ID
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getChildAccounts(MEMBER_ID, CHILD_ID)
        );

        assertEquals(ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode());
    }

    @Test
    void hidesAccountNumberDecryptionFailure() {
        byte[] ciphertext = {9, 9, 9};
        allowParentAccess();
        when(financialAccountMapper.findActiveChildAccounts(CHILD_ID))
                .thenReturn(List.of(
                        accountRow(2L, ciphertext, "1250000.00")
                ));
        when(accountNumberProtector.decrypt(ciphertext))
                .thenThrow(new IllegalArgumentException("secret"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getChildAccounts(MEMBER_ID, CHILD_ID)
        );

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode());
    }

    private void allowParentAccess() {
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID, CHILD_ID
        )).thenReturn(1);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID, CHILD_ID
        )).thenReturn(0);
    }

    private ChildAccountListRow accountRow(
            long accountId,
            byte[] ciphertext,
            String balance
    ) {
        ChildAccountListRow row = new ChildAccountListRow();
        ReflectionTestUtils.setField(row, "accountId", accountId);
        ReflectionTestUtils.setField(row, "accountName", "아이사랑적금1");
        ReflectionTestUtils.setField(
                row, "accountNumberCiphertext", ciphertext
        );
        ReflectionTestUtils.setField(row, "accountProductType", "SAVINGS");
        ReflectionTestUtils.setField(
                row, "balance", new BigDecimal(balance)
        );
        ReflectionTestUtils.setField(row, "isPrimary", true);
        return row;
    }
}
