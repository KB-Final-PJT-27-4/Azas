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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildAccountListServiceTest {

    private static final long MEMBER_ID = 8L;
    private static final long CHILD_ID = 3L;

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
    void returnsActiveChildAccountsForAccessibleParent() {
        byte[] ciphertext = {1, 2, 3};
        ChildAccountListRow row = accountRow(ciphertext);

        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.findActiveChildAccounts(CHILD_ID))
                .thenReturn(List.of(row));
        when(accountNumberProtector.decrypt(ciphertext))
                .thenReturn("123-4567-8901");

        ChildAccountListResult result = service.getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        );

        assertEquals(CHILD_ID, result.getChildId());
        assertEquals(1, result.getAccounts().size());
        assertEquals(
                "123-4567-8901",
                result.getAccounts().get(0).getAccountNumber()
        );
        assertTrue(result.getAccounts().get(0).isPrimary());
    }

    @Test
    void returnsAccountsForLinkedChildMember() {
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(financialAccountMapper.findActiveChildAccounts(CHILD_ID))
                .thenReturn(List.of());

        ChildAccountListResult result = service.getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        );

        assertTrue(result.getAccounts().isEmpty());
    }

    @Test
    void returnsEmptyListWhenChildHasNoAccounts() {
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.findActiveChildAccounts(CHILD_ID))
                .thenReturn(null);

        ChildAccountListResult result = service.getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        );

        assertTrue(result.getAccounts().isEmpty());
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
        verify(financialAccountMapper, never())
                .countActiveChildById(0L);
    }

    @Test
    void rejectsMemberWithoutChildAccess() {
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
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
                () -> service.getChildAccounts(MEMBER_ID, CHILD_ID)
        );

        assertEquals(
                ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void hidesAccountNumberDecryptionFailure() {
        byte[] ciphertext = {9, 9, 9};

        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.findActiveChildAccounts(CHILD_ID))
                .thenReturn(List.of(accountRow(ciphertext)));
        when(accountNumberProtector.decrypt(ciphertext))
                .thenThrow(new IllegalArgumentException("secret"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getChildAccounts(MEMBER_ID, CHILD_ID)
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    private ChildAccountListRow accountRow(byte[] ciphertext) {
        ChildAccountListRow row = new ChildAccountListRow();
        ReflectionTestUtils.setField(row, "accountId", 2L);
        ReflectionTestUtils.setField(row, "organizationCode", "004");
        ReflectionTestUtils.setField(row, "bankName", "KB국민은행");
        ReflectionTestUtils.setField(
                row,
                "accountName",
                "KB Young Youth 입출금통장"
        );
        ReflectionTestUtils.setField(
                row,
                "accountNumberCiphertext",
                ciphertext
        );
        ReflectionTestUtils.setField(
                row,
                "accountProductType",
                "DEMAND_DEPOSIT"
        );
        ReflectionTestUtils.setField(
                row,
                "balance",
                new BigDecimal("1250000.00")
        );
        ReflectionTestUtils.setField(row, "balanceUpdatedAt", LocalDateTime.of(
                2026, 8, 9, 5, 30
        ));
        ReflectionTestUtils.setField(row, "accountStatus", "ACTIVE");
        ReflectionTestUtils.setField(row, "primaryAccount", true);
        return row;
    }
}
