package com.azas.domain.finance.goal.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class FinancialGoalListResult {

    private final long childId;
    private final List<FinancialGoalListItemResult> financialGoals;
}
