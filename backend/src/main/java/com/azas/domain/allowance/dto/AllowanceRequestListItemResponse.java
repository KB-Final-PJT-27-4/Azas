package com.azas.domain.allowance.dto;

import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AllowanceRequestListItemResponse {

    @JsonProperty("allowance_request_id")
    private final Long allowanceRequestId;

    @JsonProperty("child_id")
    private final Long childId;

    @JsonProperty("requested_amount")
    private final BigDecimal requestedAmount;

    private final AllowanceRequestStatus status;

    @JsonProperty("requested_at")
    private final LocalDateTime requestedAt;

    public static AllowanceRequestListItemResponse from(
            AllowanceRequestListRow row
    ) {
        return new AllowanceRequestListItemResponse(
                row.getAllowanceRequestId(),
                row.getChildId(),
                row.getRequestedAmount(),
                row.getStatus(),
                row.getRequestedAt()
        );
    }
}