package com.azas.domain.checklist.service;

import com.azas.domain.checklist.dto.ChecklistItemCompletionResult;
import com.azas.domain.checklist.dto.ChecklistItemCompletionTargetRow;
import com.azas.domain.checklist.entity.ChecklistItemStatus;
import com.azas.domain.checklist.mapper.ChecklistMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistItemCompletionServiceTest {

    private MemberMapper memberMapper;
    private ChecklistMapper checklistMapper;
    private ChecklistItemCompletionService service;

    @BeforeEach
    void setUp() {
        memberMapper = mock(MemberMapper.class);
        checklistMapper = mock(ChecklistMapper.class);

        service = new ChecklistItemCompletionService(
                memberMapper,
                checklistMapper
        );
    }

    @Test
    void 연결된_보호자가_항목을_완료한다() {
        Member parent = activeParent();

        ChecklistItemCompletionTargetRow pending =
                new ChecklistItemCompletionTargetRow(
                        31L,
                        6L,
                        ChecklistItemStatus.PENDING,
                        null
                );

        LocalDateTime completedAt =
                LocalDateTime.of(2026, 8, 19, 10, 30);

        ChecklistItemCompletionTargetRow completed =
                new ChecklistItemCompletionTargetRow(
                        31L,
                        6L,
                        ChecklistItemStatus.COMPLETED,
                        completedAt
                );

        when(memberMapper.findById(7L))
                .thenReturn(parent);
        when(checklistMapper
                .findCompletionTargetForUpdate(31L))
                .thenReturn(pending);
        when(checklistMapper
                .countActiveParentAccess(7L, 6L))
                .thenReturn(1);
        when(checklistMapper.updateCompletion(
                31L,
                "COMPLETED",
                7L
        )).thenReturn(1);
        when(checklistMapper.findCompletionTargetById(31L))
                .thenReturn(completed);

        ChecklistItemCompletionResult result =
                service.updateCompletion(7L, 31L, true);

        assertEquals(31L, result.getChecklistItemId());
        assertEquals(
                ChecklistItemStatus.COMPLETED,
                result.getStatus()
        );
        assertTrue(result.isCompleted());
        assertEquals(completedAt, result.getCompletedAt());

        verify(checklistMapper).updateCompletion(
                31L,
                "COMPLETED",
                7L
        );
    }

    @Test
    void 완료된_항목을_완료_취소한다() {
        Member parent = activeParent();

        ChecklistItemCompletionTargetRow completed =
                new ChecklistItemCompletionTargetRow(
                        31L,
                        6L,
                        ChecklistItemStatus.COMPLETED,
                        LocalDateTime.of(
                                2026, 8, 19, 10, 30
                        )
                );

        ChecklistItemCompletionTargetRow pending =
                new ChecklistItemCompletionTargetRow(
                        31L,
                        6L,
                        ChecklistItemStatus.PENDING,
                        null
                );

        when(memberMapper.findById(7L))
                .thenReturn(parent);
        when(checklistMapper
                .findCompletionTargetForUpdate(31L))
                .thenReturn(completed);
        when(checklistMapper
                .countActiveParentAccess(7L, 6L))
                .thenReturn(1);
        when(checklistMapper.updateCompletion(
                31L,
                "PENDING",
                null
        )).thenReturn(1);
        when(checklistMapper.findCompletionTargetById(31L))
                .thenReturn(pending);

        ChecklistItemCompletionResult result =
                service.updateCompletion(7L, 31L, false);

        assertEquals(
                ChecklistItemStatus.PENDING,
                result.getStatus()
        );
        assertFalse(result.isCompleted());
        assertEquals(null, result.getCompletedAt());

        verify(checklistMapper).updateCompletion(
                31L,
                "PENDING",
                null
        );
    }

    @Test
    void 동일한_완료_상태_요청은_완료시각을_변경하지_않는다() {
        Member parent = activeParent();

        LocalDateTime completedAt =
                LocalDateTime.of(2026, 8, 19, 10, 30);

        ChecklistItemCompletionTargetRow completed =
                new ChecklistItemCompletionTargetRow(
                        31L,
                        6L,
                        ChecklistItemStatus.COMPLETED,
                        completedAt
                );

        when(memberMapper.findById(7L))
                .thenReturn(parent);
        when(checklistMapper
                .findCompletionTargetForUpdate(31L))
                .thenReturn(completed);
        when(checklistMapper
                .countActiveParentAccess(7L, 6L))
                .thenReturn(1);
        when(checklistMapper.findCompletionTargetById(31L))
                .thenReturn(completed);

        ChecklistItemCompletionResult result =
                service.updateCompletion(7L, 31L, true);

        assertTrue(result.isCompleted());
        assertEquals(completedAt, result.getCompletedAt());

        verify(checklistMapper, never()).updateCompletion(
                31L,
                "COMPLETED",
                7L
        );
    }

    @Test
    void 연결되지_않은_보호자는_변경할_수_없다() {
        Member parent = activeParent();

        ChecklistItemCompletionTargetRow target =
                new ChecklistItemCompletionTargetRow(
                        31L,
                        6L,
                        ChecklistItemStatus.PENDING,
                        null
                );

        when(memberMapper.findById(7L))
                .thenReturn(parent);
        when(checklistMapper
                .findCompletionTargetForUpdate(31L))
                .thenReturn(target);
        when(checklistMapper
                .countActiveParentAccess(7L, 6L))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.updateCompletion(
                        7L,
                        31L,
                        true
                )
        );

        assertEquals(
                ErrorCode.CHECKLIST_ITEM_ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(checklistMapper, never()).updateCompletion(
                31L,
                "COMPLETED",
                7L
        );
    }

    @Test
    void 존재하지_않는_항목이면_404_오류가_발생한다() {
        Member parent = activeParent();

        when(memberMapper.findById(7L))
                .thenReturn(parent);
        when(checklistMapper
                .findCompletionTargetForUpdate(999L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.updateCompletion(
                        7L,
                        999L,
                        true
                )
        );

        assertEquals(
                ErrorCode.CHECKLIST_ITEM_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    private Member activeParent() {
        Member parent = mock(Member.class);

        when(parent.getStatus())
                .thenReturn(MemberStatus.ACTIVE);
        when(parent.getMemberType())
                .thenReturn(MemberType.PARENT);

        return parent;
    }
}