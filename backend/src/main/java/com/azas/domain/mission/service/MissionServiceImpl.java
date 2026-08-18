package com.azas.domain.mission.service;

import com.azas.domain.mission.dto.CreateMissionRequest;
import com.azas.domain.mission.dto.MissionCreateResponse;
import com.azas.domain.mission.dto.MissionInsertCommand;
import com.azas.domain.mission.entity.MissionStatus;
import com.azas.domain.mission.mapper.MissionMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.azas.domain.mission.dto.*;
import com.azas.domain.mission.entity.MissionListFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class MissionServiceImpl implements MissionService {

    private final MissionMapper missionMapper;
    private final Clock clock;
    private static final int DEFAULT_LIST_SIZE = 20;
    private static final int MAX_LIST_SIZE = 100;

    @Autowired
    public MissionServiceImpl(
            MissionMapper missionMapper
    ) {
        this(
                missionMapper,
                Clock.systemUTC()
        );
    }

    MissionServiceImpl(
            MissionMapper missionMapper,
            Clock clock
    ) {
        this.missionMapper = missionMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public MissionCreateResponse createMission(
            Long memberId,
            Long childId,
            CreateMissionRequest request
    ) {
        validateRequest(childId, request);

        if (missionMapper.findActiveChildId(childId) == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        if (missionMapper.countParentAccess(
                memberId,
                childId
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.PARENT_ACCESS_REQUIRED
            );
        }

        LocalDateTime createdAt =
                LocalDateTime.now(clock);

        MissionInsertCommand command =
                new MissionInsertCommand(
                        null,
                        childId,
                        memberId,
                        request.getTitle().trim(),
                        request.getDescription().trim(),
                        request.getRewardAmount(),
                        createdAt
                );

        if (missionMapper.insertMission(command) != 1
                || command.getMissionId() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        /*
         * 자녀 회원 계정이 연결된 경우에만 알림이 저장된다.
         * 알림 INSERT가 0건이어도 미션 생성은 성공한다.
         */
        missionMapper.insertMissionAssignedNotification(
                command.getMissionId(),
                childId,
                command.getTitle(),
                command.getRewardAmount(),
                createdAt
        );

        return new MissionCreateResponse(
                command.getMissionId(),
                childId,
                command.getTitle(),
                command.getDescription(),
                command.getRewardAmount(),
                MissionStatus.ASSIGNED,
                createdAt.toInstant(ZoneOffset.UTC)
        );
    }

    private void validateRequest(
            Long childId,
            CreateMissionRequest request
    ) {
        if (childId == null
                || childId <= 0
                || request == null
                || request.getTitle() == null
                || request.getTitle().trim().isEmpty()
                || request.getTitle().trim().length() > 100
                || request.getDescription() == null
                || request.getDescription().trim().isEmpty()
                || request.getDescription().trim().length() > 1000
                || request.getRewardAmount() == null
                || request.getRewardAmount().signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_MISSION
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MissionListResponse getMissions(
            Long memberId,
            Long childId,
            String filterValue,
            String cursorValue,
            Integer sizeValue
    ) {
        if (childId == null || childId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }

        if (missionMapper.findActiveChildId(childId) == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        if (missionMapper.countMissionAccess(
                memberId,
                childId
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.CHILD_ACCESS_DENIED
            );
        }

        MissionListFilter filter =
                parseMissionFilter(filterValue);

        Long cursorId =
                parseMissionCursor(cursorValue);

        int pageSize =
                normalizeMissionListSize(sizeValue);

        MissionListQuery query =
                new MissionListQuery(
                        childId,
                        filter,
                        cursorId,
                        pageSize + 1
                );

        List<MissionListRow> rows =
                missionMapper.findMissions(query);

        if (rows == null) {
            rows = new ArrayList<>();
        }

        boolean hasNext =
                rows.size() > pageSize;

        List<MissionListRow> pageRows =
                hasNext
                        ? new ArrayList<>(
                        rows.subList(0, pageSize)
                )
                        : new ArrayList<>(rows);

        List<MissionListItemResponse> items =
                pageRows.stream()
                        .map(MissionListItemResponse::from)
                        .toList();

        Long nextCursor =
                hasNext && !pageRows.isEmpty()
                        ? pageRows.get(
                        pageRows.size() - 1
                ).getMissionId()
                        : null;

        MissionSummaryRow summaryRow =
                missionMapper.findMissionSummary(childId);

        return new MissionListResponse(
                MissionListSummaryResponse.from(
                        summaryRow
                ),
                items,
                nextCursor,
                hasNext
        );
    }

    private MissionListFilter parseMissionFilter(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return MissionListFilter.ALL;
        }

        try {
            return MissionListFilter.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }
    }

    private Long parseMissionCursor(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            long cursor = Long.parseLong(value);

            if (cursor <= 0) {
                throw new NumberFormatException();
            }

            return cursor;
        } catch (NumberFormatException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }
    }

    private int normalizeMissionListSize(
            Integer value
    ) {
        if (value == null) {
            return DEFAULT_LIST_SIZE;
        }

        if (value < 1 || value > MAX_LIST_SIZE) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }

        return value;
    }

    @Override
    @Transactional(readOnly = true)
    public MissionDetailResponse getMissionDetail(
            Long memberId,
            Long missionId
    ) {
        if (missionId == null || missionId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }

        MissionDetailRow row =
                missionMapper.findMissionDetail(
                        missionId
                );

        if (row == null) {
            throw new BusinessException(
                    ErrorCode.MISSION_NOT_FOUND
            );
        }

        if (missionMapper.countMissionAccess(
                memberId,
                row.getChildId()
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.MISSION_ACCESS_DENIED
            );
        }

        return MissionDetailResponse.from(row);
    }
}