package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.transfer.entity.TransferStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AutoTransferRetryRow {

    private Long financialTransferId;
    private Long originId;
    private Long retryOfTransferId;
    private TransferStatus status;
    private String failureCode;
    private String failureMessage;
    private String idempotencyKey;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
}