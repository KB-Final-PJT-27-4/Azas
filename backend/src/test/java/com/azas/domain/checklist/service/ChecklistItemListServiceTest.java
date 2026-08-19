package com.azas.domain.checklist.service;

import com.azas.domain.checklist.dto.ChecklistChildLifecycleRow;
import com.azas.domain.checklist.dto.ChecklistItemListResult;
import com.azas.domain.checklist.dto.ChecklistItemRow;
import com.azas.domain.checklist.entity.ChecklistLifecycleStage;
import com.azas.domain.checklist.mapper.ChecklistMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ChecklistItemListServiceTest {

    private static final Long MEMBER_ID = 7L;
    private static final Long CHILD_ID = 6L;

    private MemberMapper memberMapper;
    private ChecklistMapper checklistMapper;
    private ChecklistItemListService service;

    @BeforeEach
    void setUp() {
        memberMapper = mock(MemberMapper.class);
        checklistMapper = mock(ChecklistMapper.class);

        Clock clock = Clock.fixed(
                Instant.parse("2026-08-19T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        service = new ChecklistItemListService(
                memberMapper,
                checklistMapper,
                clock
        );
    }

    @Test
    void 연결된_부모가_선택한_단계의_체크리스트를_조회한다()
            throws Exception {
        Member parent = Member.createParent(
                "parent@example.com",
                "부모",
                null
        );

        ChecklistChildLifecycleRow child =
                childRow(
                        "BORN",
                        null,
                        LocalDate.of(2020, 5, 1)
                );

        List<ChecklistItemRow> rows = List.of(
                itemRow(1L, 101L, "항목 1", "설명 1", "PENDING"),
                itemRow(2L, 102L, "항목 2", "설명 2", "PENDING"),
                itemRow(3L, 103L, "항목 3", "설명 3", "PENDING"),
                itemRow(4L, 104L, "항목 4", "설명 4", "PENDING"),
                itemRow(5L, 105L, "항목 5", "설명 5", "PENDING"),
                itemRow(6L, 106L, "항목 6", "설명 6", "COMPLETED"),
                itemRow(7L, 107L, "항목 7", "설명 7", "COMPLETED")
        );

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(parent);
        when(checklistMapper.findActiveChildLifecycle(CHILD_ID))
                .thenReturn(child);
        when(checklistMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(checklistMapper.findItems(
                CHILD_ID,
                "AGE_5_TO_7"
        )).thenReturn(rows);

        ChecklistItemListResult result =
                service.getChecklistItems(
                        MEMBER_ID,
                        CHILD_ID,
                        "AGE_5_TO_7"
                );

        assertEquals(
                ChecklistLifecycleStage.AGE_5_TO_7,
                result.getLifecycleStage()
        );
        assertEquals(7, result.getTotalCount());
        assertEquals(2, result.getCompletedCount());
        assertEquals(29, result.getProgressPercent());
        assertFalse(result.isStageCompleted());

        verify(checklistMapper).insertMissingItems(
                CHILD_ID,
                "AGE_5_TO_7"
        );
    }

    @Test
    void stage를_생략하면_자녀_나이로_현재_단계를_계산한다()
            throws Exception {
        Member parent = Member.createParent(
                "parent@example.com",
                "부모",
                null
        );

        ChecklistChildLifecycleRow child =
                childRow(
                        "BORN",
                        null,
                        LocalDate.of(2020, 8, 18)
                );

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(parent);
        when(checklistMapper.findActiveChildLifecycle(CHILD_ID))
                .thenReturn(child);
        when(checklistMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(checklistMapper.findItems(
                CHILD_ID,
                "AGE_5_TO_7"
        )).thenReturn(List.of());

        ChecklistItemListResult result =
                service.getChecklistItems(
                        MEMBER_ID,
                        CHILD_ID,
                        null
                );

        assertEquals(
                ChecklistLifecycleStage.AGE_5_TO_7,
                result.getLifecycleStage()
        );

        verify(checklistMapper).insertMissingItems(
                CHILD_ID,
                "AGE_5_TO_7"
        );
    }

    @Test
    void 연결되지_않은_부모는_조회할_수_없다()
            throws Exception {
        Member parent = Member.createParent(
                "parent@example.com",
                "부모",
                null
        );

        ChecklistChildLifecycleRow child =
                childRow(
                        "BORN",
                        null,
                        LocalDate.of(2020, 5, 1)
                );

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(parent);
        when(checklistMapper.findActiveChildLifecycle(CHILD_ID))
                .thenReturn(child);
        when(checklistMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getChecklistItems(
                        MEMBER_ID,
                        CHILD_ID,
                        "AGE_5_TO_7"
                )
        );

        assertEquals(
                ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void 올바르지_않은_단계는_거부한다()
            throws Exception {
        Member parent = Member.createParent(
                "parent@example.com",
                "부모",
                null
        );

        ChecklistChildLifecycleRow child =
                childRow(
                        "BORN",
                        null,
                        LocalDate.of(2020, 5, 1)
                );

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(parent);
        when(checklistMapper.findActiveChildLifecycle(CHILD_ID))
                .thenReturn(child);
        when(checklistMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getChecklistItems(
                        MEMBER_ID,
                        CHILD_ID,
                        "UNKNOWN"
                )
        );

        assertEquals(
                ErrorCode.INVALID_CHECKLIST_STAGE,
                exception.getErrorCode()
        );
    }

    private ChecklistChildLifecycleRow childRow(
            String birthStatus,
            LocalDate expectedBirthDate,
            LocalDate birthDate
    ) throws Exception {
        ChecklistChildLifecycleRow row =
                new ChecklistChildLifecycleRow();

        setField(
                row,
                "birthStatus",
                Enum.valueOf(
                        com.azas.domain.child.entity.BirthStatus.class,
                        birthStatus
                )
        );
        setField(row, "expectedBirthDate", expectedBirthDate);
        setField(row, "birthDate", birthDate);

        return row;
    }

    private ChecklistItemRow itemRow(
            Long itemId,
            Long templateId,
            String title,
            String description,
            String status
    ) throws Exception {
        ChecklistItemRow row = new ChecklistItemRow();

        setField(row, "checklistItemId", itemId);
        setField(row, "checklistItemTemplateId", templateId);
        setField(row, "title", title);
        setField(row, "description", description);
        setField(
                row,
                "status",
                Enum.valueOf(
                        com.azas.domain.checklist.entity.ChecklistItemStatus.class,
                        status
                )
        );

        return row;
    }

    private void setField(
            Object target,
            String fieldName,
            Object value
    ) throws Exception {
        Field field =
                target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}