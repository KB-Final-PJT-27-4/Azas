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

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class MissionServiceImpl implements MissionService {

    private final MissionMapper missionMapper;
    private final Clock clock;

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
}