package com.azas.domain.dashboard.mapper;

import com.azas.domain.dashboard.dto.ChildDashboardAccountRow;
import com.azas.domain.dashboard.dto.ChildDashboardActivityRow;
import com.azas.domain.dashboard.dto.ChildDashboardChildRow;
import com.azas.domain.dashboard.dto.ChildDashboardMissionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChildDashboardMapper {

    ChildDashboardChildRow findActiveChildByMemberId(
            @Param("memberId") long memberId
    );

    ChildDashboardAccountRow findPrimaryAccountUsage(
            @Param("childId") long childId,
            @Param("startOccurredAt") LocalDateTime startOccurredAt,
            @Param("endOccurredAtExclusive")
            LocalDateTime endOccurredAtExclusive
    );

    ChildDashboardActivityRow findActivitySummary(
            @Param("childId") long childId,
            @Param("accountId") Long accountId,
            @Param("startOccurredAt") LocalDateTime startOccurredAt,
            @Param("endOccurredAtExclusive")
            LocalDateTime endOccurredAtExclusive
    );

    int countActiveMissions(
            @Param("childId") long childId
    );

    List<ChildDashboardMissionRow> findMissionPreview(
            @Param("childId") long childId,
            @Param("limit") int limit
    );

    long countUnreadNotifications(
            @Param("memberId") long memberId
    );
}
