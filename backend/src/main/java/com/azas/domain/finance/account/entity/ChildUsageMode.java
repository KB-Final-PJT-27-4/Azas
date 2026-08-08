package com.azas.domain.finance.account.entity;

public enum ChildUsageMode {

    CO_MANAGED,
    UNRESTRICTED;

    public boolean requiresMonthlyBudgetAmount() {
        return this == CO_MANAGED;
    }
}