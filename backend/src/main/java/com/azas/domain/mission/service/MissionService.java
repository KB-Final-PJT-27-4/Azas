package com.azas.domain.mission.service;

import com.azas.domain.mission.dto.CreateMissionRequest;
import com.azas.domain.mission.dto.MissionCreateResponse;
import com.azas.domain.mission.dto.MissionListResponse;

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
}