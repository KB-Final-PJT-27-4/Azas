package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import com.azas.domain.finance.transfer.entity.TransferStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AutoTransferScheduleDetailRow {

    private Long autoTransferScheduleId;
    private Long childId;
    private Long memberId;
    private Long financialGoalId;
    private String goalTitle;

    private Long sourceAccountId;
    private String sourceAccountName;

    private Long destinationAccountId;
    private String destinationAccountName;

    private BigDecimal amount;
    private AutoTransferFrequency frequency;
    private Integer transferDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime nextTransferAt;

    private Long lastTransferId;
    private TransferStatus lastTransferStatus;
    private String lastFailureCode;
    private String lastFailureMessage;
    private LocalDateTime lastTransferredAt;

    private AutoTransferScheduleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
