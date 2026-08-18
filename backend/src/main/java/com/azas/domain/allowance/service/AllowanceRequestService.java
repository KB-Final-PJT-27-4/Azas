package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.*;

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

    AllowanceRequestDetailResponse updateAllowanceRequestStatus(
            Long memberId,
            Long allowanceRequestId,
            UpdateAllowanceRequestStatus request
    );
}