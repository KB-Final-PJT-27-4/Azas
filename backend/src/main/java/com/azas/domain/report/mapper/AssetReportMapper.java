package com.azas.domain.report.mapper;

import com.azas.domain.report.dto.AssetReportDetailRow;
import com.azas.domain.report.dto.AssetReportListQuery;
import com.azas.domain.report.dto.AssetReportListRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AssetReportMapper {

    Long findActiveChildId(
            @Param("childId") Long childId
    );

    int countParentAccess(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    List<AssetReportListRow> findAssetReports(
            AssetReportListQuery query
    );

    AssetReportDetailRow findAssetReportDetail(
            @Param("childId") Long childId,
            @Param("reportMonth") LocalDate reportMonth
    );
}