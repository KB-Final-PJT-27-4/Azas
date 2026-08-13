package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AccountLinkResponse {
    @JsonProperty("linked_count") private final int linkedCount;
    private final List<LinkedAccountResponse> accounts;
    @JsonProperty("goal_setup_account_ids") private final List<Long> goalSetupAccountIds;

    public static AccountLinkResponse from(AccountLinkResult result) {
        List<LinkedAccountResponse> accounts = result.getAccounts()
                .stream().map(LinkedAccountResponse::from).toList();
        return new AccountLinkResponse(
                accounts.size(), accounts, result.getGoalSetupAccountIds()
        );
    }
}
