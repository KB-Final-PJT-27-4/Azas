package com.azas.domain.finance.autotransfer.service;

import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleResponse;
import com.azas.domain.finance.autotransfer.dto.CreateAutoTransferScheduleRequest;

public interface AutoTransferScheduleService {

    AutoTransferScheduleResponse createSchedule(
            Long memberId,
            String idempotencyKey,
            CreateAutoTransferScheduleRequest request
    );
}