package com.azas.domain.child.service;

import com.azas.domain.child.dto.PregnancyStatusResponse;

public interface PregnancyStatusService {

    PregnancyStatusResponse getPregnancyStatus(
            Long memberId,
            Long childId
    );
}