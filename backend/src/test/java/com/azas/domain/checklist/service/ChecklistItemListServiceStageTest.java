package com.azas.domain.checklist.service;

import com.azas.domain.checklist.dto.ChecklistChildLifecycleRow;
import com.azas.domain.checklist.dto.ChecklistItemListResult;
import com.azas.domain.checklist.entity.ChecklistLifecycleStage;
import com.azas.domain.checklist.mapper.ChecklistMapper;
import com.azas.domain.child.entity.BirthStatus;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ChecklistItemListServiceStageTest {

    private MemberMapper memberMapper;
    private ChecklistMapper checklistMapper;
    private ChecklistItemListService service;

    @BeforeEach
    void setUp() {
        memberMapper = mock(MemberMapper.class);
        checklistMapper = mock(ChecklistMapper.class);

        Clock clock = Clock.fixed(
                Instant.parse("2026-08-20T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        service = new ChecklistItemListService(
                memberMapper,
                checklistMapper,
                clock
        );
    }

    @Test
    void 열아홉살_자녀는_미래_자산_완성_단계이다() {
        ChecklistChildLifecycleRow child =
                new ChecklistChildLifecycleRow();

        ReflectionTestUtils.setField(
                child,
                "birthStatus",
                BirthStatus.BORN
        );

        ReflectionTestUtils.setField(
                child,
                "birthDate",
                LocalDate.of(2006, 8, 21)
        );

        Member parent = mock(Member.class);

        when(parent.getStatus())
                .thenReturn(MemberStatus.ACTIVE);
        when(parent.getMemberType())
                .thenReturn(MemberType.PARENT);
        when(memberMapper.findById(4L))
                .thenReturn(parent);

        when(checklistMapper.findActiveChildLifecycle(6L))
                .thenReturn(child);
        when(checklistMapper.countActiveParentAccess(4L, 6L))
                .thenReturn(1);
        when(checklistMapper.findItems(
                6L,
                "AGE_17_TO_19"
        )).thenReturn(List.of());

        ChecklistItemListResult result =
                service.getChecklistItems(
                        4L,
                        6L,
                        null
                );

        assertEquals(
                ChecklistLifecycleStage.AGE_17_TO_19,
                result.getLifecycleStage()
        );

        verify(checklistMapper).insertMissingItems(
                6L,
                "AGE_17_TO_19"
        );
    }
}