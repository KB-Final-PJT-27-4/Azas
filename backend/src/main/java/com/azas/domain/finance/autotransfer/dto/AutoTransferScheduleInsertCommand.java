package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AutoTransferScheduleInsertCommand {

    private Long autoTransferScheduleId;
    private final Long childId;
    private final Long memberId;
    private final String requestIdempotencyKey;
    private final Long financialGoalId;
    private final Long sourceAccountId;
    private final Long destinationAccountId;
    private final BigDecimal amount;
    private final AutoTransferFrequency frequency;
    private final Integer transferDay;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime nextTransferAt;
    private final LocalDateTime createdAt;
}