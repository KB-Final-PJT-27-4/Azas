package com.azas.domain.allowance.dto;

import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AllowanceRequestListQuery {

    private final Long childId;
    private final AllowanceRequestStatus status;
    private final Long cursorId;
    private final int limit;
}