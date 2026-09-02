package com.azas.domain.report.mapper;

import com.azas.domain.report.dto.ChildcareMonthlyExpenseRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChildcareReportMapper {

    Long findActiveChildId(
            @Param("childId") Long childId
    );

    int countParentAccess(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    List<ChildcareMonthlyExpenseRow> findMonthlyExpenses(
            @Param("childId") Long childId,
            @Param("startOccurredAt") LocalDateTime startOccurredAt,
            @Param("endOccurredAtExclusive")
            LocalDateTime endOccurredAtExclusive
    );
}
