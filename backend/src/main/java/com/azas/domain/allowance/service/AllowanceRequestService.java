package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.AllowanceRequestListResponse;
import com.azas.domain.allowance.dto.AllowanceRequestResponse;
import com.azas.domain.allowance.dto.CreateAllowanceRequest;

public interface AllowanceRequestService {

    AllowanceRequestResponse createAllowanceRequest(
            Long memberId,
            CreateAllowanceRequest request
    );

    AllowanceRequestListResponse getAllowanceRequests(
            Long memberId,
            Long childId,
            String status,
            String cursor,
            String size
    );
}