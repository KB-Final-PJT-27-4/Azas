package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AutoTransferScheduleRow {

    private Long autoTransferScheduleId;
    private Long childId;
    private Long memberId;
    private String requestIdempotencyKey;
    private Long financialGoalId;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private AutoTransferFrequency frequency;
    private Integer transferDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime nextTransferAt;
    private String lastTransferStatus;
    private LocalDateTime lastTransferredAt;
    private AutoTransferScheduleStatus status;
    private LocalDateTime createdAt;
}