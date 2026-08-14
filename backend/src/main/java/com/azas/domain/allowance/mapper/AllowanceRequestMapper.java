package com.azas.domain.allowance.mapper;

import com.azas.domain.allowance.dto.AllowanceRequestInsertCommand;
import com.azas.domain.allowance.dto.AllowanceRequestListQuery;
import com.azas.domain.allowance.dto.AllowanceRequestListRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.azas.domain.allowance.dto.AllowanceRequestDetailRow;

import java.util.List;

@Mapper
public interface AllowanceRequestMapper {

    Long findActiveChildIdByMemberId(
            @Param("memberId") Long memberId
    );

    int insertAllowanceRequest(
            AllowanceRequestInsertCommand command
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
}