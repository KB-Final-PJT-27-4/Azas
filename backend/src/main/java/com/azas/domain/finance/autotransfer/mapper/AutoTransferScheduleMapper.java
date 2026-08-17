package com.azas.domain.finance.autotransfer.mapper;

import com.azas.domain.finance.autotransfer.dto.AutoTransferAccountRow;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleInsertCommand;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper
public interface AutoTransferScheduleMapper {

    int countChildAccess(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );

    AutoTransferScheduleRow findByIdempotencyKey(
            @Param("idempotencyKey") String idempotencyKey
    );

    AutoTransferAccountRow findAccountForUpdate(
            @Param("accountId") Long accountId
    );

    int countEquivalentSchedule(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId,
            @Param("sourceAccountId") Long sourceAccountId,
            @Param("destinationAccountId") Long destinationAccountId,
            @Param("amount") BigDecimal amount,
            @Param("frequency") String frequency,
            @Param("transferDay") Integer transferDay,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    int insertSchedule(AutoTransferScheduleInsertCommand command);
}