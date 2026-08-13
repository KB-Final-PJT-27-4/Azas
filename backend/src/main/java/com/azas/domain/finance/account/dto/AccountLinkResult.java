package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AccountLinkResult {
    private final List<LinkedAccountResult> accounts;
    private final List<Long> goalSetupAccountIds;
}
