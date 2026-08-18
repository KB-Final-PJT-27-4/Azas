package com.azas.domain.mission.service;

import com.azas.domain.mission.dto.CreateMissionRequest;
import com.azas.domain.mission.dto.MissionCreateResponse;

public interface MissionService {

    MissionCreateResponse createMission(
            Long memberId,
            Long childId,
            CreateMissionRequest request
    );
}