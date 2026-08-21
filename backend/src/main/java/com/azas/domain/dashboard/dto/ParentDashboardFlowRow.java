package com.azas.domain.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ParentDashboardFlowRow {

    private String reportMonth;
    private BigDecimal totalAssetAmount;
}