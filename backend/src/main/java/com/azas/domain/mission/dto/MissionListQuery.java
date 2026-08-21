package com.azas.domain.mission.dto;

import com.azas.domain.mission.entity.MissionListFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionListQuery {

    private final Long childId;
    private final MissionListFilter filter;
    private final Long cursorId;
    private final int limit;
}