package com.azas.domain.allowance.mapper;

import com.azas.domain.allowance.dto.AllowanceRequestInsertCommand;
import com.azas.domain.allowance.dto.AllowanceRequestListQuery;
import com.azas.domain.allowance.dto.AllowanceRequestListRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.azas.domain.allowance.dto.AllowanceRequestDetailRow;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;

@Mapper
public interface AllowanceRequestMapper {

    Long findActiveChildIdByMemberId(
            @Param("memberId") Long memberId
    );

    int insertAllowanceRequest(
            AllowanceRequestInsertCommand command
    );

    int insertAllowanceRequestedNotification(
            @Param("allowanceRequestId") Long allowanceRequestId,
            @Param("childId") Long childId,
            @Param("requestedAmount") BigDecimal requestedAmount,
            @Param("message") String message,
            @Param("createdAt") LocalDateTime createdAt
    );

    int insertAllowanceStatusNotification(
            @Param("allowanceRequestId") Long allowanceRequestId,
            @Param("childId") Long childId,
            @Param("notificationType") String notificationType,
            @Param("title") String title,
            @Param("content") String content,
            @Param("createdAt") LocalDateTime createdAt
    );

    Long findActiveChildIdById(
            @Param("childId") Long childId
    );

    int countAllowanceRequestAccess(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    List<AllowanceRequestListRow> findAllowanceRequests(
            AllowanceRequestListQuery query
    );

    AllowanceRequestDetailRow findAllowanceRequestDetail(
            @Param("allowanceRequestId") Long allowanceRequestId
    );

    int countAllowanceRequestParentAccess(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    int countAllowanceRequestChildAccess(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    int updateAllowanceRequestStatus(
            @Param("allowanceRequestId") Long allowanceRequestId,
            @Param("status") AllowanceRequestStatus status,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
