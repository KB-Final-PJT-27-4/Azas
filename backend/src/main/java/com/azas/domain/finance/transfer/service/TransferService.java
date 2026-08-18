package com.azas.domain.finance.transfer.service;

import com.azas.domain.finance.transfer.dto.ChildTransferListItemResponse;
import com.azas.domain.finance.transfer.dto.CreateTransferRequest;
import com.azas.domain.finance.transfer.dto.MemberTransferListItemResponse;
import com.azas.domain.finance.transfer.dto.TransferCreateResponse;
import com.azas.domain.finance.transfer.dto.TransferDetailResponse;
import com.azas.domain.finance.transfer.dto.TransferListResponse;

import java.time.LocalDate;

public interface TransferService {

    TransferCreateResponse createTransfer(
            Long memberId,
            String idempotencyKey,
            CreateTransferRequest request
    );

    TransferListResponse<ChildTransferListItemResponse> getChildTransfers(
            Long memberId,
            Long childId,
            Long financialGoalId,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            String cursor,
            Integer size
    );

    TransferListResponse<MemberTransferListItemResponse> getMemberTransfers(
            Long memberId,
            String status,
            String transferType,
            Long childId,
            LocalDate startDate,
            LocalDate endDate,
            String cursor,
            Integer size
    );

    TransferDetailResponse getTransfer(Long memberId, Long transferId);
}