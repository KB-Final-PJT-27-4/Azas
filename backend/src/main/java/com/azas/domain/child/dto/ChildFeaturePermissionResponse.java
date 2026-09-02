package com.azas.domain.child.dto;

import com.azas.domain.child.entity.ChildFeaturePermission;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChildFeaturePermissionResponse {

    private final Long childId;

    private final boolean allowanceRequestEnabled;

    private final boolean usageLimitViewEnabled;

    @JsonProperty("child_id")
    public Long getChildId() {
        return childId;
    }

    @JsonProperty("allowance_request_enabled")
    public boolean isAllowanceRequestEnabled() {
        return allowanceRequestEnabled;
    }

    @JsonProperty("usage_limit_view_enabled")
    public boolean isUsageLimitViewEnabled() {
        return usageLimitViewEnabled;
    }

    public static ChildFeaturePermissionResponse from(
            ChildFeaturePermission permission
    ) {
        return new ChildFeaturePermissionResponse(
                permission.getChildId(),
                permission.isAllowanceRequestEnabled(),
                permission.isUsageLimitViewEnabled()
        );
    }
}
