package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AutoTransferScheduleListRow {

    private Long autoTransferScheduleId;
    private Long financialGoalId;
    private String goalTitle;
    private BigDecimal amount;
    private AutoTransferFrequency frequency;
    private Integer transferDay;
    private LocalDateTime nextTransferAt;
    private String lastTransferStatus;
    private LocalDateTime lastTransferredAt;
    private AutoTransferScheduleStatus status;
}