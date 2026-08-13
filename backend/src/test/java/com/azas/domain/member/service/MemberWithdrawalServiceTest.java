package com.azas.domain.member.service;

import com.azas.domain.auth.service.RefreshTokenStore;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.domain.member.mapper.MemberWithdrawalMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberWithdrawalServiceTest {

    private static final long MEMBER_ID = 1L;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private MemberWithdrawalMapper memberWithdrawalMapper;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    private MemberWithdrawalService memberWithdrawalService;

    @BeforeEach
    void setUp() {
        memberWithdrawalService =
                new MemberWithdrawalService(
                        memberMapper,
                        refreshTokenStore,
                        memberWithdrawalMapper
                );
    }

    @Test
    void withdrawsParentAndRemovesLoginInformation() {
        Member member = activeParent();

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);

        when(memberWithdrawalMapper
                .countChildrenWithNoOtherActiveGuardian(MEMBER_ID))
                .thenReturn(0);

        when(refreshTokenStore.revokeAllActiveByMemberId(
                eq(MEMBER_ID),
                any(LocalDateTime.class)
        )).thenReturn(2);

        when(memberMapper.anonymizeAndWithdrawIfActive(
                eq(MEMBER_ID),
                any(LocalDateTime.class)
        )).thenReturn(1);

        memberWithdrawalService.withdrawMyMembership(MEMBER_ID);

        InOrder callOrder = inOrder(
                memberMapper,
                memberWithdrawalMapper,
                refreshTokenStore
        );

        callOrder.verify(memberMapper)
                .findById(MEMBER_ID);

        callOrder.verify(memberWithdrawalMapper)
                .countChildrenWithNoOtherActiveGuardian(MEMBER_ID);

        callOrder.verify(refreshTokenStore)
                .revokeAllActiveByMemberId(
                        eq(MEMBER_ID),
                        any(LocalDateTime.class)
                );

        callOrder.verify(memberWithdrawalMapper)
                .cancelPendingInvitationsByInviter(
                        eq(MEMBER_ID),
                        any(LocalDateTime.class)
                );

        callOrder.verify(memberWithdrawalMapper)
                .revokeActiveFinancialConnections(
                        eq(MEMBER_ID),
                        any(LocalDateTime.class)
                );

        callOrder.verify(memberWithdrawalMapper)
                .unlinkChildMember(MEMBER_ID);

        callOrder.verify(memberWithdrawalMapper)
                .deleteChildParentRelations(MEMBER_ID);

        callOrder.verify(memberWithdrawalMapper)
                .deletePhoneVerifications(MEMBER_ID);

        callOrder.verify(memberWithdrawalMapper)
                .deleteSocialAccounts(MEMBER_ID);

        callOrder.verify(memberMapper)
                .anonymizeAndWithdrawIfActive(
                        eq(MEMBER_ID),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void withdrawsChildAndUnlinksChildProfile() {
        Member member = activeChild();

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);

        when(memberMapper.anonymizeAndWithdrawIfActive(
                eq(MEMBER_ID),
                any(LocalDateTime.class)
        )).thenReturn(1);

        memberWithdrawalService.withdrawMyMembership(MEMBER_ID);

        verify(memberWithdrawalMapper)
                .unlinkChildMember(MEMBER_ID);

        verify(memberWithdrawalMapper)
                .deleteSocialAccounts(MEMBER_ID);

        verify(memberMapper)
                .anonymizeAndWithdrawIfActive(
                        eq(MEMBER_ID),
                        any(LocalDateTime.class)
                );

        // 자녀 회원은 보호자 고립 여부를 검사하지 않는다.
        verify(memberWithdrawalMapper, never())
                .countChildrenWithNoOtherActiveGuardian(MEMBER_ID);
    }

    @Test
    void completesWhenMemberHasNoActiveRefreshToken() {
        Member member = activeParent();

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);

        when(memberWithdrawalMapper
                .countChildrenWithNoOtherActiveGuardian(MEMBER_ID))
                .thenReturn(0);

        when(refreshTokenStore.revokeAllActiveByMemberId(
                eq(MEMBER_ID),
                any(LocalDateTime.class)
        )).thenReturn(0);

        when(memberMapper.anonymizeAndWithdrawIfActive(
                eq(MEMBER_ID),
                any(LocalDateTime.class)
        )).thenReturn(1);

        assertDoesNotThrow(
                () -> memberWithdrawalService
                        .withdrawMyMembership(MEMBER_ID)
        );

        verify(memberMapper)
                .anonymizeAndWithdrawIfActive(
                        eq(MEMBER_ID),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void rejectsLastActiveGuardianWithdrawal() {
        Member member = activeParent();

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);

        when(memberWithdrawalMapper
                .countChildrenWithNoOtherActiveGuardian(MEMBER_ID))
                .thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberWithdrawalService
                        .withdrawMyMembership(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.LAST_GUARDIAN_WITHDRAWAL_NOT_ALLOWED,
                exception.getErrorCode()
        );

        verifyNoInteractions(refreshTokenStore);

        verify(memberWithdrawalMapper, never())
                .deleteSocialAccounts(MEMBER_ID);

        verify(memberWithdrawalMapper, never())
                .deleteChildParentRelations(MEMBER_ID);

        verify(memberMapper, never())
                .anonymizeAndWithdrawIfActive(
                        eq(MEMBER_ID),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void rejectsMissingMember() {
        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberWithdrawalService
                        .withdrawMyMembership(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.INVALID_ACCESS_TOKEN,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                memberWithdrawalMapper,
                refreshTokenStore
        );

        verify(memberMapper, never())
                .anonymizeAndWithdrawIfActive(
                        eq(MEMBER_ID),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void rejectsAlreadyWithdrawnMember() {
        Member member = activeParent();

        ReflectionTestUtils.setField(
                member,
                "status",
                MemberStatus.WITHDRAWN
        );

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberWithdrawalService
                        .withdrawMyMembership(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.WITHDRAWN_MEMBER,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                memberWithdrawalMapper,
                refreshTokenStore
        );

        verify(memberMapper, never())
                .anonymizeAndWithdrawIfActive(
                        eq(MEMBER_ID),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void rejectsConcurrentWithdrawal() {
        Member member = activeParent();

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);

        when(memberWithdrawalMapper
                .countChildrenWithNoOtherActiveGuardian(MEMBER_ID))
                .thenReturn(0);

        when(memberMapper.anonymizeAndWithdrawIfActive(
                eq(MEMBER_ID),
                any(LocalDateTime.class)
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberWithdrawalService
                        .withdrawMyMembership(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.WITHDRAWN_MEMBER,
                exception.getErrorCode()
        );

        verify(memberMapper)
                .anonymizeAndWithdrawIfActive(
                        eq(MEMBER_ID),
                        any(LocalDateTime.class)
                );
    }

    private Member activeParent() {
        Member member = Member.createParent(
                "parent@example.com",
                "김하나",
                null
        );

        ReflectionTestUtils.setField(
                member,
                "memberId",
                MEMBER_ID
        );

        return member;
    }

    private Member activeChild() {
        Member member = Member.createChild(
                "child@example.com",
                "김자녀",
                null
        );

        ReflectionTestUtils.setField(
                member,
                "memberId",
                MEMBER_ID
        );

        return member;
    }
}