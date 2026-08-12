package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountPrimaryTargetRow;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountPrimaryServiceTest {

    private static final long MEMBER_ID = 8L;
    private static final long CHILD_ID = 6L;
    private static final long ACCOUNT_ID = 3L;

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    @InjectMocks
    private AccountPrimaryService service;

    @Test
    void setsParentAccountAsPrimaryForConnectionOwner() {
        AccountPrimaryTargetRow target = target(
                "PARENT",
                MEMBER_ID,
                null,
                false
        );
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target);
        when(financialAccountMapper.setPrimaryAccountForParentScope(
                MEMBER_ID,
                ACCOUNT_ID
        )).thenReturn(2);

        service.setPrimaryAccount(MEMBER_ID, ACCOUNT_ID);

        verify(financialAccountMapper)
                .setPrimaryAccountForParentScope(
                        MEMBER_ID,
                        ACCOUNT_ID
                );
        verify(financialAccountMapper, never())
                .setPrimaryAccountForChildScope(
                        CHILD_ID,
                        ACCOUNT_ID
                );
    }

    @Test
    void connectedParentSetsChildAccountAsPrimary() {
        AccountPrimaryTargetRow target = target(
                "CHILD",
                MEMBER_ID,
                CHILD_ID,
                false
        );
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.setPrimaryAccountForChildScope(
                CHILD_ID,
                ACCOUNT_ID
        )).thenReturn(2);

        service.setPrimaryAccount(MEMBER_ID, ACCOUNT_ID);

        verify(financialAccountMapper)
                .setPrimaryAccountForChildScope(
                        CHILD_ID,
                        ACCOUNT_ID
                );
    }

    @Test
    void childMemberSetsOwnAccountAsPrimary() {
        AccountPrimaryTargetRow target = target(
                "CHILD",
                1L,
                CHILD_ID,
                false
        );
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);
        when(financialAccountMapper.countActiveChildMemberAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(financialAccountMapper.setPrimaryAccountForChildScope(
                CHILD_ID,
                ACCOUNT_ID
        )).thenReturn(1);

        service.setPrimaryAccount(MEMBER_ID, ACCOUNT_ID);

        verify(financialAccountMapper)
                .setPrimaryAccountForChildScope(
                        CHILD_ID,
                        ACCOUNT_ID
                );
    }

    @Test
    void repeatedRequestIsIdempotent() {
        AccountPrimaryTargetRow target = target(
                "PARENT",
                MEMBER_ID,
                null,
                true
        );
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target);

        service.setPrimaryAccount(MEMBER_ID, ACCOUNT_ID);

        verify(financialAccountMapper, never())
                .setPrimaryAccountForParentScope(
                        MEMBER_ID,
                        ACCOUNT_ID
                );
    }

    @Test
    void rejectsInvalidAccountId() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.setPrimaryAccount(MEMBER_ID, 0L)
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
        verify(financialAccountMapper, never())
                .findAccountPrimaryTargetByIdForUpdate(0L);
    }

    @Test
    void returnsNotFoundForMissingAccount() {
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.setPrimaryAccount(
                        MEMBER_ID,
                        ACCOUNT_ID
                )
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsInactiveAccount() {
        AccountPrimaryTargetRow target = target(
                "PARENT",
                MEMBER_ID,
                null,
                false
        );
        ReflectionTestUtils.setField(
                target,
                "accountStatus",
                "CLOSED"
        );
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target);

        assertNotFound(target);
    }

    @Test
    void rejectsUnlinkedAccount() {
        AccountPrimaryTargetRow target = target(
                "PARENT",
                MEMBER_ID,
                null,
                false
        );
        ReflectionTestUtils.setField(
                target,
                "linkStatus",
                "UNLINKED"
        );
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target);

        assertNotFound(target);
    }

    @Test
    void rejectsInactiveConsent() {
        AccountPrimaryTargetRow target = target(
                "PARENT",
                MEMBER_ID,
                null,
                false
        );
        ReflectionTestUtils.setField(
                target,
                "consentStatus",
                "REVOKED"
        );
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target);

        assertNotFound(target);
    }

    @Test
    void rejectsDifferentParentAccountOwner() {
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target(
                        "PARENT",
                        99L,
                        null,
                        false
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.setPrimaryAccount(
                        MEMBER_ID,
                        ACCOUNT_ID
                )
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsInaccessibleChildAccount() {
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target(
                        "CHILD",
                        1L,
                        CHILD_ID,
                        false
                ));
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
                () -> service.setPrimaryAccount(
                        MEMBER_ID,
                        ACCOUNT_ID
                )
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsUnknownOwnerTypeAsInternalDataError() {
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target(
                        "UNKNOWN",
                        MEMBER_ID,
                        null,
                        false
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.setPrimaryAccount(
                        MEMBER_ID,
                        ACCOUNT_ID
                )
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsFailedPrimaryUpdate() {
        when(financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target(
                        "PARENT",
                        MEMBER_ID,
                        null,
                        false
                ));
        when(financialAccountMapper.setPrimaryAccountForParentScope(
                MEMBER_ID,
                ACCOUNT_ID
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.setPrimaryAccount(
                        MEMBER_ID,
                        ACCOUNT_ID
                )
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    private void assertNotFound(AccountPrimaryTargetRow ignored) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.setPrimaryAccount(
                        MEMBER_ID,
                        ACCOUNT_ID
                )
        );
        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    private AccountPrimaryTargetRow target(
            String ownerType,
            Long connectedByMemberId,
            Long childId,
            boolean primaryAccount
    ) {
        AccountPrimaryTargetRow target =
                new AccountPrimaryTargetRow();
        ReflectionTestUtils.setField(
                target,
                "accountId",
                ACCOUNT_ID
        );
        ReflectionTestUtils.setField(
                target,
                "ownerType",
                ownerType
        );
        ReflectionTestUtils.setField(
                target,
                "connectedByMemberId",
                connectedByMemberId
        );
        ReflectionTestUtils.setField(
                target,
                "childId",
                childId
        );
        ReflectionTestUtils.setField(
                target,
                "accountStatus",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(
                target,
                "linkStatus",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(
                target,
                "consentStatus",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(
                target,
                "primaryAccount",
                primaryAccount
        );
        return target;
    }
}
