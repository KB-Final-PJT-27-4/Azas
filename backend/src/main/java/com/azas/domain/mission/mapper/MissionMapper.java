package com.azas.domain.mission.mapper;

import com.azas.domain.mission.dto.*;
import com.azas.domain.mission.entity.MissionStatus;
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

    MissionDetailRow findMissionDetailForUpdate(
            @Param("missionId") Long missionId
    );

    int countChildSelfAccess(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    int updateMissionStatus(
            @Param("missionId") Long missionId,
            @Param("status") MissionStatus status,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    int linkRewardTransfer(
            @Param("transferId") Long transferId,
            @Param("missionId") Long missionId,
            @Param("memberId") Long memberId
    );

    int insertMissionSubmittedNotification(
            @Param("missionId") Long missionId,
            @Param("childId") Long childId,
            @Param("missionTitle") String missionTitle,
            @Param("createdAt") LocalDateTime createdAt
    );

    int insertChildMissionStatusNotification(
            @Param("missionId") Long missionId,
            @Param("childId") Long childId,
            @Param("notificationType") String notificationType,
            @Param("title") String title,
            @Param("content") String content,
            @Param("createdAt") LocalDateTime createdAt
    );
}