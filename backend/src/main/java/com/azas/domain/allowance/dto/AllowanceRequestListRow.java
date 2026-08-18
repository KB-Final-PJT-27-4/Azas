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
public class AllowanceRequestListRow {

    private Long allowanceRequestId;
    private Long childId;
    private BigDecimal requestedAmount;
    private AllowanceRequestStatus status;
    private LocalDateTime requestedAt;
}