package com.azas.domain.mission.service;

import com.azas.domain.finance.transfer.service.TransferService;
import com.azas.domain.mission.dto.CreateMissionRequest;
import com.azas.domain.mission.dto.MissionInsertCommand;
import com.azas.domain.mission.entity.MissionStatus;
import com.azas.domain.mission.mapper.MissionMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.azas.domain.finance.transfer.dto.CreateTransferRequest;
import com.azas.domain.finance.transfer.dto.TransferCreateResponse;
import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.finance.transfer.entity.TransferType;
import com.azas.domain.mission.dto.MissionDetailRow;
import com.azas.domain.mission.dto.UpdateMissionStatusRequest;
import com.azas.domain.mission.entity.MissionAction;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.azas.domain.mission.dto.*;
import com.azas.domain.mission.entity.MissionListFilter;

import java.time.LocalDateTime;
import java.util.List;

class MissionServiceImplTest {

    private MissionMapper missionMapper;
    private MissionServiceImpl missionService;
    private TransferService transferService;
    private static final Instant FIXED_NOW =
            Instant.parse("2026-08-18T03:00:00Z");

    @BeforeEach
    void setUp() {
        missionMapper = mock(MissionMapper.class);
        transferService = mock(TransferService.class);

        Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);

        missionService =
                new MissionServiceImpl(
                        missionMapper,
                        transferService,
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
                FIXED_NOW,
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

    // 조회 성공 테스트
    @Test
    void 부모가_자녀_미션_목록을_조회한다() {
        when(missionMapper.findActiveChildId(6L))
                .thenReturn(6L);

        when(missionMapper.countMissionAccess(
                7L,
                6L
        )).thenReturn(1);

        MissionListRow first =
                missionRow(
                        13L,
                        MissionStatus.SUBMITTED
                );

        MissionListRow second =
                missionRow(
                        12L,
                        MissionStatus.ASSIGNED
                );

        MissionListRow extra =
                missionRow(
                        11L,
                        MissionStatus.APPROVED
                );

        when(missionMapper.findMissions(any()))
                .thenReturn(
                        List.of(first, second, extra)
                );

        MissionSummaryRow summary =
                new MissionSummaryRow();

        summary.setTotalCount(3);
        summary.setInProgressCount(1);
        summary.setNeedsReviewCount(1);
        summary.setCompletedCount(1);

        when(missionMapper.findMissionSummary(6L))
                .thenReturn(summary);

        MissionListResponse response =
                missionService.getMissions(
                        7L,
                        6L,
                        "ALL",
                        null,
                        2
                );

        assertEquals(2, response.getItems().size());
        assertTrue(response.isHasNext());
        assertEquals(12L, response.getNextCursor());

        assertEquals(
                3,
                response.getSummary().getTotalCount()
        );

        assertEquals(
                MissionStatus.SUBMITTED,
                response.getItems().get(0).getStatus()
        );

        verify(missionMapper).findMissions(
                argThat(query ->
                        query.getChildId().equals(6L)
                                && query.getFilter()
                                == MissionListFilter.ALL
                                && query.getCursorId() == null
                                && query.getLimit() == 3
                )
        );
    }

    private MissionListRow missionRow(
            Long missionId,
            MissionStatus status
    ) {
        MissionListRow row =
                new MissionListRow();

        row.setMissionId(missionId);
        row.setChildId(6L);
        row.setTitle("미션 " + missionId);
        row.setDescription("미션 내용");
        row.setRewardAmount(
                new BigDecimal("2000")
        );
        row.setStatus(status);
        row.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        8,
                        18,
                        1,
                        0
                )
        );

        return row;
    }

    // 조회 접근 거부 테스트
    @Test
    void 연결되지_않은_회원은_미션_목록을_조회할_수_없다() {
        when(missionMapper.findActiveChildId(6L))
                .thenReturn(6L);

        when(missionMapper.countMissionAccess(
                99L,
                6L
        )).thenReturn(0);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> missionService.getMissions(
                                99L,
                                6L,
                                null,
                                null,
                                null
                        )
                );

        assertEquals(
                ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(missionMapper, never())
                .findMissions(any());
    }

    // 잘못된 필터 테스트
    @Test
    void 올바르지_않은_미션_필터는_거부한다() {
        when(missionMapper.findActiveChildId(6L))
                .thenReturn(6L);

        when(missionMapper.countMissionAccess(
                7L,
                6L
        )).thenReturn(1);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> missionService.getMissions(
                                7L,
                                6L,
                                "UNKNOWN",
                                null,
                                20
                        )
                );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );
    }


    // 미션 상세 조회 성공테스트
    @Test
    void 연결된_부모가_미션_상세를_조회한다() {
        MissionDetailRow row =
                missionDetailRow();

        when(missionMapper.findMissionDetail(13L))
                .thenReturn(row);

        when(missionMapper.countMissionAccess(
                7L,
                6L
        )).thenReturn(1);

        var response =
                missionService.getMissionDetail(
                        7L,
                        13L
                );

        assertEquals(
                13L,
                response.getMissionId()
        );
        assertEquals(
                6L,
                response.getChildId()
        );
        assertEquals(
                "소비 계획 지키기",
                response.getTitle()
        );
        assertEquals(
                "이번 주 계획한 소비 지키기",
                response.getDescription()
        );
        assertEquals(
                new BigDecimal("2000"),
                response.getRewardAmount()
        );
        assertEquals(
                MissionStatus.SUBMITTED,
                response.getStatus()
        );
        assertEquals(
                Instant.parse(
                        "2026-08-18T01:00:00Z"
                ),
                response.getCreatedAt()
        );
        assertEquals(
                Instant.parse(
                        "2026-08-18T02:00:00Z"
                ),
                response.getUpdatedAt()
        );
    }

    // 미션 상세 조회 미션 없음 테스트
    @Test
    void 존재하지_않는_미션_상세는_조회할_수_없다() {
        when(missionMapper.findMissionDetail(999L))
                .thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> missionService
                                .getMissionDetail(
                                        7L,
                                        999L
                                )
                );

        assertEquals(
                ErrorCode.MISSION_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(missionMapper, never())
                .countMissionAccess(
                        anyLong(),
                        anyLong()
                );
    }

    private MissionDetailRow missionDetailRow() {
        MissionDetailRow row =
                new MissionDetailRow();

        row.setMissionId(13L);
        row.setChildId(6L);
        row.setTitle("소비 계획 지키기");
        row.setDescription(
                "이번 주 계획한 소비 지키기"
        );
        row.setRewardAmount(
                new BigDecimal("2000")
        );
        row.setStatus(
                MissionStatus.SUBMITTED
        );
        row.setCreatedAt(
                LocalDateTime.of(
                        2026, 8, 18, 1, 0
                )
        );
        row.setUpdatedAt(
                LocalDateTime.of(
                        2026, 8, 18, 2, 0
                )
        );

        return row;
    }


    // 자녀 제출 테스트
    @Test
    void 자녀가_미션_완료를_요청한다() {
        MissionDetailRow row =
                missionDetailRow(
                        MissionStatus.ASSIGNED
                );

        when(missionMapper.findMissionDetailForUpdate(13L))
                .thenReturn(row);

        when(missionMapper.countChildSelfAccess(
                20L,
                6L
        )).thenReturn(1);

        when(missionMapper.updateMissionStatus(
                eq(13L),
                eq(MissionStatus.SUBMITTED),
                any()
        )).thenReturn(1);

        var response =
                missionService.updateMissionStatus(
                        20L,
                        13L,
                        new UpdateMissionStatusRequest(
                                MissionAction.SUBMIT,
                                null,
                                null
                        )
                );

        assertEquals(
                MissionStatus.SUBMITTED,
                response.getStatus()
        );

        verify(missionMapper)
                .insertMissionSubmittedNotification(
                        eq(13L),
                        eq(6L),
                        eq("소비 계획 지키기"),
                        any()
                );

        verifyNoInteractions(transferService);
    }

    // 부모 승인 및 보상 테스트
    @Test
    void 부모가_제출된_미션을_승인하고_보상한다() {
        MissionDetailRow row =
                missionDetailRow(
                        MissionStatus.SUBMITTED
                );

        when(missionMapper.findMissionDetailForUpdate(13L))
                .thenReturn(row);

        when(missionMapper.countParentAccess(
                7L,
                6L
        )).thenReturn(1);

        when(transferService.createTransfer(
                eq(7L),
                anyString(),
                any(CreateTransferRequest.class)
        )).thenReturn(
                new TransferCreateResponse(
                        81L,
                        null,
                        null,
                        null,
                        new BigDecimal("2000"),
                        "소비 계획 지키기 미션 보상",
                        TransferType.MANUAL,
                        TransferStatus.SUCCEEDED,
                        Instant.parse(
                                "2026-08-18T03:00:00Z"
                        )
                )
        );

        when(missionMapper.linkRewardTransfer(
                81L,
                13L,
                7L
        )).thenReturn(1);

        when(missionMapper.updateMissionStatus(
                eq(13L),
                eq(MissionStatus.APPROVED),
                any()
        )).thenReturn(1);

        var response =
                missionService.updateMissionStatus(
                        7L,
                        13L,
                        new UpdateMissionStatusRequest(
                                MissionAction.APPROVE,
                                1L,
                                12L
                        )
                );

        assertEquals(
                MissionStatus.APPROVED,
                response.getStatus()
        );

        verify(transferService).createTransfer(
                eq(7L),
                anyString(),
                argThat(request ->
                        request.getSourceAccountId()
                                .equals(1L)
                                && request
                                .getDestinationAccountId()
                                .equals(12L)
                                && request.getAmount()
                                .compareTo(
                                        new BigDecimal("2000")
                                ) == 0
                )
        );

        verify(missionMapper).linkRewardTransfer(
                81L,
                13L,
                7L
        );
    }

    // 잘못된 상태 전이 테스트
    @Test
    void 승인된_미션은_다시_제출할_수_없다() {
        when(missionMapper.findMissionDetailForUpdate(13L))
                .thenReturn(
                        missionDetailRow(
                                MissionStatus.APPROVED
                        )
                );

        when(missionMapper.countChildSelfAccess(
                20L,
                6L
        )).thenReturn(1);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> missionService
                                .updateMissionStatus(
                                        20L,
                                        13L,
                                        new UpdateMissionStatusRequest(
                                                MissionAction.SUBMIT,
                                                null,
                                                null
                                        )
                                )
                );

        assertEquals(
                ErrorCode.INVALID_MISSION_STATUS_TRANSITION,
                exception.getErrorCode()
        );

        verify(missionMapper, never())
                .updateMissionStatus(
                        anyLong(),
                        any(),
                        any()
                );
    }

    // 부모가 아닌 다른 회원의 승인 방지
    @Test
    void 자녀는_미션을_승인할_수_없다() {
        when(missionMapper.findMissionDetailForUpdate(13L))
                .thenReturn(
                        missionDetailRow(
                                MissionStatus.SUBMITTED
                        )
                );

        when(missionMapper.countParentAccess(
                20L,
                6L
        )).thenReturn(0);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> missionService
                                .updateMissionStatus(
                                        20L,
                                        13L,
                                        new UpdateMissionStatusRequest(
                                                MissionAction.APPROVE,
                                                1L,
                                                12L
                                        )
                                )
                );

        assertEquals(
                ErrorCode.MISSION_ACCESS_DENIED,
                exception.getErrorCode()
        );

        verifyNoInteractions(transferService);
    }
    private MissionDetailRow missionDetailRow(
            MissionStatus status
    ) {
        MissionDetailRow row =
                new MissionDetailRow();

        row.setMissionId(13L);
        row.setChildId(6L);
        row.setTitle("소비 계획 지키기");
        row.setDescription(
                "이번 주 계획한 소비 지키기"
        );
        row.setRewardAmount(
                new BigDecimal("2000")
        );
        row.setStatus(status);
        row.setCreatedAt(
                LocalDateTime.of(
                        2026, 8, 18, 1, 0
                )
        );
        row.setUpdatedAt(
                LocalDateTime.of(
                        2026, 8, 18, 2, 0
                )
        );

        return row;
    }


}