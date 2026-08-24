package com.azas.domain.notification.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UsageLimitNotificationTarget {
    private Long childId;
    private Long accountId;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
}
