package com.azas.domain.mission.service;

import com.azas.domain.mission.dto.CreateMissionRequest;
import com.azas.domain.mission.dto.MissionInsertCommand;
import com.azas.domain.mission.entity.MissionStatus;
import com.azas.domain.mission.mapper.MissionMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MissionServiceImplTest {

    private MissionMapper missionMapper;
    private MissionServiceImpl missionService;

    @BeforeEach
    void setUp() {
        missionMapper = mock(MissionMapper.class);

        Clock clock = Clock.fixed(
                Instant.parse("2026-08-18T01:00:00Z"),
                ZoneOffset.UTC
        );

        missionService =
                new MissionServiceImpl(
                        missionMapper,
                        clock
                );
    }

    @Test
    void 연결된_부모가_자녀_미션을_생성한다() {
        when(missionMapper.findActiveChildId(6L))
                .thenReturn(6L);
        when(missionMapper.countParentAccess(7L, 6L))
                .thenReturn(1);

        when(missionMapper.insertMission(any()))
                .thenAnswer(invocation -> {
                    MissionInsertCommand command =
                            invocation.getArgument(0);

                    command.setMissionId(71L);
                    return 1;
                });

        when(missionMapper
                .insertMissionAssignedNotification(
                        anyLong(),
                        anyLong(),
                        anyString(),
                        any(),
                        any()
                ))
                .thenReturn(1);

        var response =
                missionService.createMission(
                        7L,
                        6L,
                        createRequest()
                );

        assertEquals(71L, response.getMissionId());
        assertEquals(6L, response.getChildId());
        assertEquals(
                "일주일 동안 방 정리하기",
                response.getTitle()
        );
        assertEquals(
                "매일 자기 전에 책상과 바닥을 정리해요.",
                response.getDescription()
        );
        assertEquals(
                new BigDecimal("5000"),
                response.getRewardAmount()
        );
        assertEquals(
                MissionStatus.ASSIGNED,
                response.getStatus()
        );
        assertEquals(
                Instant.parse("2026-08-18T01:00:00Z"),
                response.getCreatedAt()
        );

        verify(missionMapper)
                .insertMissionAssignedNotification(
                        eq(71L),
                        eq(6L),
                        eq("일주일 동안 방 정리하기"),
                        eq(new BigDecimal("5000")),
                        any()
                );
    }

    @Test
    void 존재하지_않는_자녀에게는_미션을_생성할_수_없다() {
        when(missionMapper.findActiveChildId(999L))
                .thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> missionService.createMission(
                                7L,
                                999L,
                                createRequest()
                        )
                );

        assertEquals(
                ErrorCode.CHILD_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(missionMapper, never())
                .insertMission(any());
    }

    @Test
    void 연결되지_않은_부모는_미션을_생성할_수_없다() {
        when(missionMapper.findActiveChildId(6L))
                .thenReturn(6L);
        when(missionMapper.countParentAccess(99L, 6L))
                .thenReturn(0);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> missionService.createMission(
                                99L,
                                6L,
                                createRequest()
                        )
                );

        assertEquals(
                ErrorCode.PARENT_ACCESS_REQUIRED,
                exception.getErrorCode()
        );

        verify(missionMapper, never())
                .insertMission(any());
    }

    @Test
    void 미션_이름이_공백이면_생성할_수_없다() {
        CreateMissionRequest request =
                createRequest();

        ReflectionTestUtils.setField(
                request,
                "title",
                " "
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> missionService.createMission(
                                7L,
                                6L,
                                request
                        )
                );

        assertEquals(
                ErrorCode.INVALID_MISSION,
                exception.getErrorCode()
        );

        verify(missionMapper, never())
                .findActiveChildId(anyLong());
    }

    @Test
    void 미션_내용이_공백이면_생성할_수_없다() {
        CreateMissionRequest request =
                createRequest();

        ReflectionTestUtils.setField(
                request,
                "description",
                " "
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> missionService.createMission(
                                7L,
                                6L,
                                request
                        )
                );

        assertEquals(
                ErrorCode.INVALID_MISSION,
                exception.getErrorCode()
        );
    }

    @Test
    void 완료_보상이_0원이면_생성할_수_없다() {
        CreateMissionRequest request =
                createRequest();

        ReflectionTestUtils.setField(
                request,
                "rewardAmount",
                BigDecimal.ZERO
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> missionService.createMission(
                                7L,
                                6L,
                                request
                        )
                );

        assertEquals(
                ErrorCode.INVALID_MISSION,
                exception.getErrorCode()
        );
    }

    @Test
    void 자녀_회원이_연결되지_않아도_미션은_생성된다() {
        when(missionMapper.findActiveChildId(6L))
                .thenReturn(6L);
        when(missionMapper.countParentAccess(7L, 6L))
                .thenReturn(1);

        when(missionMapper.insertMission(any()))
                .thenAnswer(invocation -> {
                    MissionInsertCommand command =
                            invocation.getArgument(0);

                    command.setMissionId(71L);
                    return 1;
                });

        /*
         * 자녀 프로필의 member_id가 NULL이면
         * 알림 INSERT ... SELECT 결과는 0건이다.
         */
        when(missionMapper
                .insertMissionAssignedNotification(
                        anyLong(),
                        anyLong(),
                        anyString(),
                        any(),
                        any()
                ))
                .thenReturn(0);

        var response =
                missionService.createMission(
                        7L,
                        6L,
                        createRequest()
                );

        assertEquals(71L, response.getMissionId());
        assertEquals(
                MissionStatus.ASSIGNED,
                response.getStatus()
        );
    }

    private CreateMissionRequest createRequest() {
        CreateMissionRequest request =
                new CreateMissionRequest();

        ReflectionTestUtils.setField(
                request,
                "title",
                "일주일 동안 방 정리하기"
        );
        ReflectionTestUtils.setField(
                request,
                "description",
                "매일 자기 전에 책상과 바닥을 정리해요."
        );
        ReflectionTestUtils.setField(
                request,
                "rewardAmount",
                new BigDecimal("5000")
        );

        return request;
    }
}