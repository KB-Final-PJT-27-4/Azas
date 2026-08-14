package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.AllowanceRequestListResponse;
import com.azas.domain.allowance.dto.AllowanceRequestResponse;
import com.azas.domain.allowance.dto.CreateAllowanceRequest;
import com.azas.domain.allowance.dto.AllowanceRequestDetailResponse;

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

    AllowanceRequestDetailResponse getAllowanceRequestDetail(
            Long memberId,
            Long allowanceRequestId
    );
}