package com.azas.domain.finance.autotransfer.service;

import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleDetailResponse;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleListResponse;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleResponse;
import com.azas.domain.finance.autotransfer.dto.CreateAutoTransferScheduleRequest;

public interface AutoTransferScheduleService {

    AutoTransferScheduleResponse createSchedule(
            Long memberId,
            String idempotencyKey,
            CreateAutoTransferScheduleRequest request
    );

    AutoTransferScheduleListResponse getSchedules(
            Long memberId,
            Long childId,
            String status,
            String cursor,
            Integer size
    );

    AutoTransferScheduleDetailResponse getScheduleDetail(
            Long memberId,
            Long scheduleId
    );
}