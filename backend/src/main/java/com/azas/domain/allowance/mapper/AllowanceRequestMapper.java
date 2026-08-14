package com.azas.domain.allowance.mapper;

import com.azas.domain.allowance.dto.AllowanceRequestInsertCommand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AllowanceRequestMapper {

    Long findActiveChildIdByMemberId(
            @Param("memberId") Long memberId
    );

    int insertAllowanceRequest(
            AllowanceRequestInsertCommand command
    );
}