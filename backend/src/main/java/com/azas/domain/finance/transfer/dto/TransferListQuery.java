package com.azas.domain.finance.transfer.dto;

import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.finance.transfer.entity.TransferType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TransferListQuery {

    private final Long memberId;
    private final Long childId;
    private final Long financialGoalTemplateId;
    private final TransferStatus status;
    private final TransferType transferType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Long cursorId;
    private final int limit;
}