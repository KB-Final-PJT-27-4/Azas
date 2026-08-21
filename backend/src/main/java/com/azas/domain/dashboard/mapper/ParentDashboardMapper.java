package com.azas.domain.dashboard.mapper;

import com.azas.domain.dashboard.dto.ParentDashboardChecklistRow;
import com.azas.domain.dashboard.dto.ParentDashboardFlowRow;
import com.azas.domain.dashboard.dto.ParentDashboardSummaryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ParentDashboardMapper {

    int countActiveChild(@Param("childId") Long childId);

    int countActiveParentAccess(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    ParentDashboardSummaryRow findDashboardSummary(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    List<ParentDashboardFlowRow> findSixMonthFlow(
            @Param("childId") Long childId
    );

    List<ParentDashboardChecklistRow> findChecklistPreview(
            @Param("childId") Long childId,
            @Param("limit") int limit
    );
}