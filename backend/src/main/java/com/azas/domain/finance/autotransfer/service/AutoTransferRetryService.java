package com.azas.domain.finance.autotransfer.service;

import com.azas.domain.finance.autotransfer.dto.AutoTransferRetryResponse;

public interface AutoTransferRetryService {

    AutoTransferRetryResponse retry(
            Long memberId,
            Long scheduleId,
            String idempotencyKey
    );
}