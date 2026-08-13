package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@RequiredArgsConstructor
public final class ParentAccountListResult {

    private final BigDecimal totalBalance;
    private final List<ParentAccountListItemResult> accounts;
}
