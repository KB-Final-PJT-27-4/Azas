package com.azas.domain.report.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ChildcareMonthlyExpenseRow {

    private LocalDate reportMonth;

    private BigDecimal expenseAmount;
}