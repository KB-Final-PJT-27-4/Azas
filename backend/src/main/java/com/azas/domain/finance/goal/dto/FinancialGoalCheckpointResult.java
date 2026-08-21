package com.azas.domain.finance.goal.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@RequiredArgsConstructor
public final class FinancialGoalCheckpointResult {

    private final long financialGoalCheckpointId;
    private final int percentage;
    private final BigDecimal targetAmount;
    private final boolean reached;
    private final Instant reachedAt;
}
