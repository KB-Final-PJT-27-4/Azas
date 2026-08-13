package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.dto.AccountDetailRow;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountDetailServiceTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 3L;
    private static final long CHILD_ID = 1L;

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    @Mock
    private AccountNumberProtector accountNumberProtector;

    private AccountDetailService service;

    @BeforeEach
    void setUp() {
        service = new AccountDetailService(
                financialAccountMapper,
                accountNumberProtector
        );
    }

    @Test
    void returnsParentOwnedAccountForConnectionOwner() {
        byte[] ciphertext = {1, 2, 3};
        AccountDetailRow row = parentAccountRow(ciphertext);

        when(financialAccountMapper.findLinkedAccountDetailById(
                ACCOUNT_ID
        )).thenReturn(row);
        when(accountNumberProtector.decrypt(ciphertext))
                .thenReturn("987-6543-5678");

        AccountDetailResult result = service.getAccountDetail(
                MEMBER_ID,
                ACCOUNT_ID
        );

        assertEquals("PARENT", result.getOwnerType());
        assertNull(result.getChild());
        assertNull(result.getFinancialGoal());
        assertEquals("987-6543-5678", result.getAccountNumber());

        verify(financialAccountMapper, never())
                .countActiveParentAccess(MEMBER_ID, CHILD_ID);
    }

    @Test
    void returnsChildAccountForAccessibleParent() {
        byte[] ciphertext = {4, 5, 6};
        AccountDetailRow row = childAccountRow(ciphertext);

        when(financialAccountMapper.findLinkedAccountDetailById(
                ACCOUNT_ID
        )).thenReturn(row);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(accountNumberProtector.decrypt(ciphertext))
                .thenReturn("123-4567-2001");

        AccountDetailResult result = service.getAccountDetail(
                MEMBER_ID,
                ACCOUNT_ID
        );

        assertEquals(CHILD_ID, result.getChild().getChildId());
        assertEquals("깨비", result.getChild().getName());
        assertEquals(
                "대학자금 마련",
                result.getFinancialGoal().getGoalName()
        );
        assertEquals(
                new BigDecimal("30000000.00"),
                result.getFinancialGoal().getTargetAmount()
        );
        assertFalse(result.isPrimary());
    }

    @Test
    void returnsChildAccountForLinkedChildMember() {
        byte[] ciphertext = {7, 8, 9};
        AccountDetailRow row = childAccountRow(ciphertext);

        when(financialAccountMapper.findLinkedAccountDetailById(
                ACCOUNT_ID
        )).thenReturn(row);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(accountNumberProtector.decrypt(ciphertext))
                .thenReturn("123-4567-2001");

        AccountDetailResult result = service.getAccountDetail(
                MEMBER_ID,
                ACCOUNT_ID
        );

        assertEquals(ACCOUNT_ID, result.getAccountId());
    }

    @Test
    void rejectsInvalidAccountId() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getAccountDetail(MEMBER_ID, 0L)
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
        verify(financialAccountMapper, never())
                .findLinkedAccountDetailById(0L);
    }

    @Test
    void returnsNotFoundForMissingOrUnlinkedAccount() {
        when(financialAccountMapper.findLinkedAccountDetailById(
                ACCOUNT_ID
        )).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getAccountDetail(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsDifferentMemberForParentAccount() {
        when(financialAccountMapper.findLinkedAccountDetailById(
                ACCOUNT_ID
        )).thenReturn(parentAccountRow(new byte[]{1}));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getAccountDetail(99L, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsMemberWithoutChildAccountAccess() {
        when(financialAccountMapper.findLinkedAccountDetailById(
                ACCOUNT_ID
        )).thenReturn(childAccountRow(new byte[]{1}));
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
                () -> service.getAccountDetail(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsInconsistentChildOwnershipData() {
        AccountDetailRow row = childAccountRow(new byte[]{1});
        ReflectionTestUtils.setField(row, "childName", null);

        when(financialAccountMapper.findLinkedAccountDetailById(
                ACCOUNT_ID
        )).thenReturn(row);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getAccountDetail(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    @Test
    void hidesAccountNumberDecryptionFailure() {
        byte[] ciphertext = {9, 9, 9};

        when(financialAccountMapper.findLinkedAccountDetailById(
                ACCOUNT_ID
        )).thenReturn(parentAccountRow(ciphertext));
        when(accountNumberProtector.decrypt(ciphertext))
                .thenThrow(new IllegalArgumentException("secret"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getAccountDetail(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    private AccountDetailRow parentAccountRow(byte[] ciphertext) {
        AccountDetailRow row = baseRow(ciphertext);
        ReflectionTestUtils.setField(row, "ownerType", "PARENT");
        ReflectionTestUtils.setField(
                row,
                "ownerMemberId",
                MEMBER_ID
        );
        return row;
    }

    private AccountDetailRow childAccountRow(byte[] ciphertext) {
        AccountDetailRow row = baseRow(ciphertext);
        ReflectionTestUtils.setField(row, "ownerType", "CHILD");
        ReflectionTestUtils.setField(row, "childId", CHILD_ID);
        ReflectionTestUtils.setField(row, "childName", "깨비");
        ReflectionTestUtils.setField(
                row,
                "goalNameSnapshot",
                "대학자금 마련"
        );
        ReflectionTestUtils.setField(
                row,
                "goalTargetAmount",
                new BigDecimal("30000000.00")
        );
        ReflectionTestUtils.setField(
                row,
                "goalTargetDate",
                LocalDate.of(2038, 1, 12)
        );
        return row;
    }

    private AccountDetailRow baseRow(byte[] ciphertext) {
        AccountDetailRow row = new AccountDetailRow();
        ReflectionTestUtils.setField(row, "accountId", ACCOUNT_ID);
        ReflectionTestUtils.setField(row, "organizationCode", "004");
        ReflectionTestUtils.setField(row, "bankName", "KB국민은행");
        ReflectionTestUtils.setField(
                row,
                "accountName",
                "KB Young Youth 적금"
        );
        ReflectionTestUtils.setField(
                row,
                "accountNumberCiphertext",
                ciphertext
        );
        ReflectionTestUtils.setField(
                row,
                "accountProductType",
                "SAVINGS"
        );
        ReflectionTestUtils.setField(
                row,
                "balance",
                new BigDecimal("14600000.00")
        );
        ReflectionTestUtils.setField(
                row,
                "balanceUpdatedAt",
                LocalDateTime.of(2026, 8, 10, 5, 30)
        );
        ReflectionTestUtils.setField(row, "accountStatus", "ACTIVE");
        ReflectionTestUtils.setField(row, "primaryAccount", false);
        ReflectionTestUtils.setField(
                row,
                "openedAt",
                LocalDateTime.of(2024, 1, 12, 9, 0)
        );
        ReflectionTestUtils.setField(
                row,
                "maturityDate",
                LocalDate.of(2038, 1, 12)
        );
        ReflectionTestUtils.setField(
                row,
                "linkedAt",
                LocalDateTime.of(2026, 8, 4, 7, 29, 20)
        );
        return row;
    }
}
