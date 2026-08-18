package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UpdateAutoTransferScheduleCommand {

    private final Long scheduleId;
    private final BigDecimal amount;
    private final Integer transferDay;
    private final LocalDate endDate;
    private final LocalDateTime nextTransferAt;
    private final AutoTransferScheduleStatus status;
    private final LocalDateTime updatedAt;
}