package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AccountLinkRequest {
    @JsonProperty("owner_type")
    private String ownerType;
    @JsonProperty("child_id")
    private Long childId;
    @JsonProperty("account_ids")
    private List<Long> accountIds;
}
