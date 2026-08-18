package com.azas.domain.mission.service;

import com.azas.domain.mission.dto.CreateMissionRequest;
import com.azas.domain.mission.dto.MissionCreateResponse;
import com.azas.domain.mission.dto.MissionDetailResponse;
import com.azas.domain.mission.dto.MissionListResponse;
import com.azas.domain.mission.dto.UpdateMissionStatusRequest;

public interface MissionService {

    MissionCreateResponse createMission(
            Long memberId,
            Long childId,
            CreateMissionRequest request
    );

    MissionListResponse getMissions(
            Long memberId,
            Long childId,
            String filter,
            String cursor,
            Integer size
    );

    MissionDetailResponse getMissionDetail(
            Long memberId,
            Long missionId
    );

    MissionDetailResponse updateMissionStatus(
            Long memberId,
            Long missionId,
            UpdateMissionStatusRequest request
    );
}