package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.goal.dto.FinancialGoalAccountTargetRow;
import com.azas.domain.finance.goal.dto.FinancialGoalUpdateTargetRow;
import com.azas.domain.finance.goal.mapper.FinancialGoalMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialGoalDeleteServiceTest {

    @Mock MemberMapper memberMapper;
    @Mock FinancialGoalMapper goalMapper;
    private FinancialGoalDeleteService service;

    @BeforeEach
    void setUp() {
        service = new FinancialGoalDeleteService(memberMapper, goalMapper);
    }

    @Test
    void archivesGoalAndUnlinksEverySavingsAccount() {
        givenParent();
        when(goalMapper.findAccessibleGoalForUpdate(31L, 8L))
                .thenReturn(mock(FinancialGoalUpdateTargetRow.class));
        when(goalMapper.findGoalAccountIds(31L)).thenReturn(List.of(11L, 12L));
        when(goalMapper.findAccountTargetsForUpdate(List.of(11L, 12L)))
                .thenReturn(List.of(
                        mock(FinancialGoalAccountTargetRow.class),
                        mock(FinancialGoalAccountTargetRow.class)));
        when(goalMapper.deleteFinancialGoalAccount(31L, 11L)).thenReturn(1);
        when(goalMapper.deleteFinancialGoalAccount(31L, 12L)).thenReturn(1);
        when(goalMapper.clearAccountGoalSnapshot(11L)).thenReturn(1);
        when(goalMapper.clearAccountGoalSnapshot(12L)).thenReturn(1);
        when(goalMapper.archiveFinancialGoal(31L)).thenReturn(1);

        service.delete(8L, 31L);

        verify(goalMapper).deleteFinancialGoalAccount(31L, 11L);
        verify(goalMapper).deleteFinancialGoalAccount(31L, 12L);
        verify(goalMapper).clearAccountGoalSnapshot(11L);
        verify(goalMapper).clearAccountGoalSnapshot(12L);
        verify(goalMapper).archiveFinancialGoal(31L);
    }

    @Test
    void archivesGoalEvenWhenNoAccountLinkRemains() {
        givenParent();
        when(goalMapper.findAccessibleGoalForUpdate(31L, 8L))
                .thenReturn(mock(FinancialGoalUpdateTargetRow.class));
        when(goalMapper.findGoalAccountIds(31L)).thenReturn(List.of());
        when(goalMapper.archiveFinancialGoal(31L)).thenReturn(1);

        service.delete(8L, 31L);

        verify(goalMapper, never()).findAccountTargetsForUpdate(
                org.mockito.ArgumentMatchers.anyList());
        verify(goalMapper).archiveFinancialGoal(31L);
    }

    @Test
    void rejectsInvalidGoalId() {
        givenParent();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.delete(8L, 0L));

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
        verify(goalMapper, never()).findAccessibleGoalForUpdate(anyLong(), anyLong());
    }

    @Test
    void hidesMissingOrInaccessibleGoal() {
        givenParent();
        when(goalMapper.findAccessibleGoalForUpdate(31L, 8L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.delete(8L, 31L));

        assertEquals(ErrorCode.FINANCIAL_GOAL_NOT_FOUND,
                exception.getErrorCode());
        verify(goalMapper, never()).archiveFinancialGoal(anyLong());
    }

    @Test
    void rejectsChildMember() {
        Member child = mock(Member.class);
        when(child.getStatus()).thenReturn(MemberStatus.ACTIVE);
        when(child.getMemberType()).thenReturn(MemberType.CHILD);
        when(memberMapper.findById(9L)).thenReturn(child);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.delete(9L, 31L));

        assertEquals(ErrorCode.PARENT_ACCESS_REQUIRED,
                exception.getErrorCode());
    }

    @Test
    void rejectsInactiveMemberToken() {
        Member member = mock(Member.class);
        when(member.getStatus()).thenReturn(MemberStatus.WITHDRAWN);
        when(memberMapper.findById(8L)).thenReturn(member);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.delete(8L, 31L));

        assertEquals(ErrorCode.INVALID_ACCESS_TOKEN,
                exception.getErrorCode());
    }

    @Test
    void failsWhenLinkedAccountCannotBeLocked() {
        givenParent();
        when(goalMapper.findAccessibleGoalForUpdate(31L, 8L))
                .thenReturn(mock(FinancialGoalUpdateTargetRow.class));
        when(goalMapper.findGoalAccountIds(31L)).thenReturn(List.of(11L));
        when(goalMapper.findAccountTargetsForUpdate(List.of(11L)))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.delete(8L, 31L));

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode());
        verify(goalMapper, never()).archiveFinancialGoal(anyLong());
    }

    private void givenParent() {
        Member parent = mock(Member.class);
        when(parent.getStatus()).thenReturn(MemberStatus.ACTIVE);
        when(parent.getMemberType()).thenReturn(MemberType.PARENT);
        when(memberMapper.findById(8L)).thenReturn(parent);
    }
}
