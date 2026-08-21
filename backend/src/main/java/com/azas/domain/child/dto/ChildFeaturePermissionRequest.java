package com.azas.domain.child.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class ChildFeaturePermissionRequest {

    @NotNull
    @JsonProperty("allowance_request_enabled")
    private Boolean allowanceRequestEnabled;

    @NotNull
    @JsonProperty("usage_limit_view_enabled")
    private Boolean usageLimitViewEnabled;
}
