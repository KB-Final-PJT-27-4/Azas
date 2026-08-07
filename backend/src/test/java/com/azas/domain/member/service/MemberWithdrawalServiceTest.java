package com.azas.domain.member.service;

import com.azas.domain.auth.service.RefreshTokenStore;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.mapper.MemberMapper;
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

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    private MemberWithdrawalService
            memberWithdrawalService;

    @BeforeEach
    void setUp() {
        memberWithdrawalService =
                new MemberWithdrawalService(
                        memberMapper,
                        refreshTokenStore
                );
    }

    @Test
    void withdrawsActiveMemberAndRevokesAllTokens() {
        Member member = activeMember();

        when(memberMapper.findById(1L))
                .thenReturn(member);
        when(memberMapper.withdrawIfActive(1L))
                .thenReturn(1);
        when(
                refreshTokenStore
                        .revokeAllActiveByMemberId(
                                eq(1L),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(2);

        memberWithdrawalService
                .withdrawMyMembership(1L);

        InOrder callOrder = inOrder(
                memberMapper,
                refreshTokenStore
        );

        callOrder.verify(memberMapper)
                .findById(1L);
        callOrder.verify(memberMapper)
                .withdrawIfActive(1L);
        callOrder.verify(refreshTokenStore)
                .revokeAllActiveByMemberId(
                        eq(1L),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void completesWhenMemberHasNoActiveRefreshToken() {
        Member member = activeMember();

        when(memberMapper.findById(1L))
                .thenReturn(member);
        when(memberMapper.withdrawIfActive(1L))
                .thenReturn(1);
        when(
                refreshTokenStore
                        .revokeAllActiveByMemberId(
                                eq(1L),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(0);

        assertDoesNotThrow(
                () -> memberWithdrawalService
                        .withdrawMyMembership(1L)
        );

        verify(refreshTokenStore)
                .revokeAllActiveByMemberId(
                        eq(1L),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void rejectsMissingMember() {
        when(memberMapper.findById(1L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberWithdrawalService
                        .withdrawMyMembership(1L)
        );

        assertEquals(
                ErrorCode.INVALID_ACCESS_TOKEN,
                exception.getErrorCode()
        );

        verify(memberMapper, never())
                .withdrawIfActive(1L);
        verifyNoInteractions(refreshTokenStore);
    }

    @Test
    void rejectsAlreadyWithdrawnMember() {
        Member member = activeMember();

        ReflectionTestUtils.setField(
                member,
                "status",
                MemberStatus.WITHDRAWN
        );

        when(memberMapper.findById(1L))
                .thenReturn(member);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberWithdrawalService
                        .withdrawMyMembership(1L)
        );

        assertEquals(
                ErrorCode.WITHDRAWN_MEMBER,
                exception.getErrorCode()
        );

        verify(memberMapper, never())
                .withdrawIfActive(1L);
        verifyNoInteractions(refreshTokenStore);
    }

    @Test
    void rejectsMemberWithdrawnByConcurrentRequest() {
        Member member = activeMember();

        when(memberMapper.findById(1L))
                .thenReturn(member);
        when(memberMapper.withdrawIfActive(1L))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberWithdrawalService
                        .withdrawMyMembership(1L)
        );

        assertEquals(
                ErrorCode.WITHDRAWN_MEMBER,
                exception.getErrorCode()
        );

        verifyNoInteractions(refreshTokenStore);
    }

    private Member activeMember() {
        Member member = Member.createParent(
                "parent@example.com",
                "김하나",
                null
        );

        ReflectionTestUtils.setField(
                member,
                "memberId",
                1L
        );

        return member;
    }
}