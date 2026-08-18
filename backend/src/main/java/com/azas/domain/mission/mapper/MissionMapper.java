package com.azas.domain.mission.mapper;

import com.azas.domain.mission.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface MissionMapper {

    Long findActiveChildId(
            @Param("childId") Long childId
    );

    int countParentAccess(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    int insertMission(
            MissionInsertCommand command
    );

    int insertMissionAssignedNotification(
            @Param("missionId") Long missionId,
            @Param("childId") Long childId,
            @Param("missionTitle") String missionTitle,
            @Param("rewardAmount") BigDecimal rewardAmount,
            @Param("createdAt") LocalDateTime createdAt
    );
    int countMissionAccess(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    List<MissionListRow> findMissions(
            MissionListQuery query
    );

    MissionSummaryRow findMissionSummary(
            @Param("childId") Long childId
    );
    MissionDetailRow findMissionDetail(
            @Param("missionId") Long missionId
    );

}