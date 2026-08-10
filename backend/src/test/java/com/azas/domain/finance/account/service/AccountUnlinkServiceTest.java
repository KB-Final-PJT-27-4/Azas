package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountUnlinkTargetRow;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountUnlinkServiceTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 3L;
    private static final Instant NOW = Instant.parse(
            "2026-08-10T06:30:00Z"
    );

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    private AccountUnlinkService service;

    @BeforeEach
    void setUp() {
        service = new AccountUnlinkService(
                financialAccountMapper,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void unlinksActiveAccountForConnectionOwner() {
        when(financialAccountMapper
                .findAccountUnlinkTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target("ACTIVE"));
        when(financialAccountMapper.unlinkAccount(
                ACCOUNT_ID,
                LocalDateTime.ofInstant(NOW, ZoneOffset.UTC)
        )).thenReturn(1);

        service.unlinkAccount(MEMBER_ID, ACCOUNT_ID);

        verify(financialAccountMapper).unlinkAccount(
                ACCOUNT_ID,
                LocalDateTime.ofInstant(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void unlinksDiscoveredAccountForConnectionOwner() {
        when(financialAccountMapper
                .findAccountUnlinkTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target("DISCOVERED"));
        when(financialAccountMapper.unlinkAccount(
                ACCOUNT_ID,
                LocalDateTime.ofInstant(NOW, ZoneOffset.UTC)
        )).thenReturn(1);

        service.unlinkAccount(MEMBER_ID, ACCOUNT_ID);

        verify(financialAccountMapper).unlinkAccount(
                ACCOUNT_ID,
                LocalDateTime.ofInstant(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void repeatedUnlinkIsIdempotentAndKeepsOriginalTimestamp() {
        AccountUnlinkTargetRow target = target("UNLINKED");
        ReflectionTestUtils.setField(
                target,
                "unlinkedAt",
                LocalDateTime.of(2026, 8, 9, 10, 0)
        );

        when(financialAccountMapper
                .findAccountUnlinkTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target);

        service.unlinkAccount(MEMBER_ID, ACCOUNT_ID);

        verify(financialAccountMapper, never()).unlinkAccount(
                ACCOUNT_ID,
                LocalDateTime.ofInstant(NOW, ZoneOffset.UTC)
        );
        assertEquals(
                LocalDateTime.of(2026, 8, 9, 10, 0),
                target.getUnlinkedAt()
        );
    }

    @Test
    void rejectsInvalidAccountId() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.unlinkAccount(MEMBER_ID, 0L)
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
        verify(financialAccountMapper, never())
                .findAccountUnlinkTargetByIdForUpdate(0L);
    }

    @Test
    void returnsNotFoundForMissingAccount() {
        when(financialAccountMapper
                .findAccountUnlinkTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.unlinkAccount(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsMemberWhoDidNotCreateConnection() {
        when(financialAccountMapper
                .findAccountUnlinkTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target("ACTIVE"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.unlinkAccount(99L, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode()
        );
        verify(financialAccountMapper, never()).unlinkAccount(
                ACCOUNT_ID,
                LocalDateTime.ofInstant(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void rejectsMissingConnectionOwnerAsInternalAccessDenial() {
        AccountUnlinkTargetRow target = target("ACTIVE");
        ReflectionTestUtils.setField(
                target,
                "connectedByMemberId",
                null
        );

        when(financialAccountMapper
                .findAccountUnlinkTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.unlinkAccount(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsUnknownLinkStatusAsInternalDataError() {
        when(financialAccountMapper
                .findAccountUnlinkTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target("UNKNOWN"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.unlinkAccount(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsConcurrentUpdateFailure() {
        when(financialAccountMapper
                .findAccountUnlinkTargetByIdForUpdate(ACCOUNT_ID))
                .thenReturn(target("ACTIVE"));
        when(financialAccountMapper.unlinkAccount(
                ACCOUNT_ID,
                LocalDateTime.ofInstant(NOW, ZoneOffset.UTC)
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.unlinkAccount(MEMBER_ID, ACCOUNT_ID)
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    private AccountUnlinkTargetRow target(String linkStatus) {
        AccountUnlinkTargetRow target = new AccountUnlinkTargetRow();
        ReflectionTestUtils.setField(target, "accountId", ACCOUNT_ID);
        ReflectionTestUtils.setField(
                target,
                "connectedByMemberId",
                MEMBER_ID
        );
        ReflectionTestUtils.setField(
                target,
                "linkStatus",
                linkStatus
        );
        return target;
    }
}
