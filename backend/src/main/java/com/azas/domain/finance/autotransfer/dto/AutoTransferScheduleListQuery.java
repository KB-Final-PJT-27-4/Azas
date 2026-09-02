package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AutoTransferScheduleListQuery {

    private final Long memberId;
    private final Long childId;
    private final AutoTransferScheduleStatus status;
    private final Long cursorId;
    private final Integer limit;
}
