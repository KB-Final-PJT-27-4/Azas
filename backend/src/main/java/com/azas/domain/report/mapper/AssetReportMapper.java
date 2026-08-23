package com.azas.domain.report.mapper;

import com.azas.domain.report.dto.AssetReportDetailRow;
import com.azas.domain.report.dto.AssetReportGoalAccountRow;
import com.azas.domain.report.dto.AssetReportListQuery;
import com.azas.domain.report.dto.AssetReportListRow;
import com.azas.domain.report.dto.AssetReportUpsertCommand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    List<Long> findAllActiveChildIds();

    BigDecimal findTotalAssetAmountAt(
            @Param("childId") Long childId,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    BigDecimal findMonthlySavedAmount(
            @Param("childId") Long childId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    BigDecimal findPreviousTotalAssetAmount(
            @Param("childId") Long childId,
            @Param("reportMonth") LocalDate reportMonth
    );

    BigDecimal findPreviousMonthlySavedAmount(
            @Param("childId") Long childId,
            @Param("reportMonth") LocalDate reportMonth
    );

    List<AssetReportGoalAccountRow> findGoalAccountSnapshots(
            @Param("childId") Long childId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    int upsertAssetReport(
            AssetReportUpsertCommand command
    );
}
