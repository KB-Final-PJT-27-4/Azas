package com.azas.domain.allowance.dto;

import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AllowanceRequestDetailRow {

    private Long allowanceRequestId;
    private Long childId;
    private BigDecimal requestedAmount;
    private String message;
    private AllowanceRequestStatus status;
    private LocalDateTime requestedAt;
}