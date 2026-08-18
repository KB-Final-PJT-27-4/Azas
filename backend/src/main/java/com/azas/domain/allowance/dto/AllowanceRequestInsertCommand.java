package com.azas.domain.allowance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AllowanceRequestInsertCommand {

    private Long allowanceRequestId;
    private Long childId;
    private BigDecimal requestedAmount;
    private String message;
    private LocalDateTime requestedAt;
}