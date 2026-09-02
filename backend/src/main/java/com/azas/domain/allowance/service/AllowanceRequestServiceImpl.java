package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.*;
import com.azas.domain.allowance.entity.AllowanceRequestAction;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.azas.domain.allowance.mapper.AllowanceRequestMapper;
import com.azas.domain.child.service.ChildFeaturePermissionService;
import com.azas.domain.finance.transfer.dto.CreateTransferRequest;
import com.azas.domain.finance.transfer.dto.TransferCreateResponse;
import com.azas.domain.finance.transfer.service.TransferService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllowanceRequestServiceImpl implements AllowanceRequestService {

    private final AllowanceRequestMapper allowanceRequestMapper;
    private final ChildFeaturePermissionService childFeaturePermissionService;
    private final TransferService transferService;
    private static final int DEFAULT_LIST_SIZE = 20;
    private static final int MAX_LIST_SIZE = 100;
    private static final String ALLOWANCE_APPROVAL_TRANSFER_MEMO =
            "용돈 요청 승인 이체";

    @Override
    @Transactional
    public AllowanceRequestResponse createAllowanceRequest(
            Long memberId,
            CreateAllowanceRequest request
    ) {
        Long childId =
                allowanceRequestMapper.findActiveChildIdByMemberId(memberId);

        if (childId == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_MEMBER_ACCESS_REQUIRED
            );
        }

        childFeaturePermissionService
                .validateAllowanceRequestEnabled(childId);

        LocalDateTime requestedAt = LocalDateTime.now();

        AllowanceRequestInsertCommand command =
                new AllowanceRequestInsertCommand(
                        null,
                        childId,
                        request.getRequestedAmount(),
                        request.getMessage().trim(),
                        requestedAt
                );

        int insertedCount =
                allowanceRequestMapper.insertAllowanceRequest(command);

        if (insertedCount != 1
                || command.getAllowanceRequestId() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        allowanceRequestMapper.insertAllowanceRequestedNotification(
                command.getAllowanceRequestId(),
                childId,
                command.getRequestedAmount(),
                command.getMessage(),
                requestedAt
        );

        return new AllowanceRequestResponse(
                command.getAllowanceRequestId(),
                childId,
                command.getRequestedAmount(),
                command.getMessage(),
                AllowanceRequestStatus.PENDING,
                requestedAt
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AllowanceRequestListResponse getAllowanceRequests(
            Long memberId,
            Long childId,
            String statusValue,
            String cursorValue,
            String sizeValue
    ) {
        if (childId == null || childId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }

        if (allowanceRequestMapper.findActiveChildIdById(childId) == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        if (allowanceRequestMapper.countAllowanceRequestAccess(
                memberId,
                childId
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.CHILD_ACCESS_DENIED
            );
        }

        AllowanceRequestStatus status =
                parseStatus(statusValue);
        Long cursorId =
                parseCursor(cursorValue);
        int pageSize =
                parseSize(sizeValue);

        AllowanceRequestListQuery query =
                new AllowanceRequestListQuery(
                        childId,
                        status,
                        cursorId,
                        pageSize + 1
                );

        List<AllowanceRequestListRow> rows =
                allowanceRequestMapper.findAllowanceRequests(query);

        List<AllowanceRequestListItemResponse> items =
                rows == null
                        ? new ArrayList<>()
                        : rows.stream()
                        .map(AllowanceRequestListItemResponse::from)
                        .collect(Collectors.toCollection(ArrayList::new));

        boolean hasNext = items.size() > pageSize;

        if (hasNext) {
            items.remove(items.size() - 1);
        }

        Long nextCursor = hasNext && !items.isEmpty()
                ? items.get(items.size() - 1)
                .getAllowanceRequestId()
                : null;

        return new AllowanceRequestListResponse(
                items,
                nextCursor,
                hasNext
        );
    }

    private AllowanceRequestStatus parseStatus(String value) {
        if (value == null) {
            return null;
        }

        if (value.isBlank()) {
            throw invalidQueryParameter();
        }

        try {
            return AllowanceRequestStatus.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw invalidQueryParameter();
        }
    }

    private Long parseCursor(String value) {
        if (value == null) {
            return null;
        }

        try {
            long cursor = Long.parseLong(value);

            if (cursor <= 0) {
                throw new NumberFormatException();
            }

            return cursor;
        } catch (NumberFormatException exception) {
            throw invalidQueryParameter();
        }
    }

    private int parseSize(String value) {
        if (value == null) {
            return DEFAULT_LIST_SIZE;
        }

        try {
            int size = Integer.parseInt(value);

            if (size < 1 || size > MAX_LIST_SIZE) {
                throw new NumberFormatException();
            }

            return size;
        } catch (NumberFormatException exception) {
            throw invalidQueryParameter();
        }
    }

    private BusinessException invalidQueryParameter() {
        return new BusinessException(
                ErrorCode.INVALID_QUERY_PARAMETER
        );
    }
    @Override
    @Transactional(readOnly = true)
    public AllowanceRequestDetailResponse getAllowanceRequestDetail(
            Long memberId,
            Long allowanceRequestId
    ) {
        if (allowanceRequestId == null || allowanceRequestId <= 0) {
            throw new BusinessException(
                    ErrorCode.BADREQUEST
            );
        }

        AllowanceRequestDetailRow row =
                allowanceRequestMapper.findAllowanceRequestDetail(
                        allowanceRequestId
                );

        if (row == null) {
            throw new BusinessException(
                    ErrorCode.ALLOWANCE_REQUEST_NOT_FOUND
            );
        }

        if (allowanceRequestMapper.countAllowanceRequestAccess(
                memberId,
                row.getChildId()
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.CHILD_ACCESS_DENIED
            );
        }

        return AllowanceRequestDetailResponse.from(row);
    }

    @Override
    @Transactional
    public AllowanceRequestDetailResponse updateAllowanceRequestStatus(
            Long memberId,
            Long allowanceRequestId,
            UpdateAllowanceRequestStatus request
    ) {
        if (allowanceRequestId == null || allowanceRequestId <= 0) {
            throw new BusinessException(
                    ErrorCode.BADREQUEST
            );
        }

        AllowanceRequestAction action =
                parseAllowanceRequestAction(request);

        AllowanceRequestDetailRow current =
                allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                        allowanceRequestId
                );

        if (current == null) {
            throw new BusinessException(
                    ErrorCode.ALLOWANCE_REQUEST_NOT_FOUND
            );
        }

        validateStatusChangeAccess(
                memberId,
                current.getChildId(),
                action
        );

        if (current.getStatus()
                != AllowanceRequestStatus.PENDING) {
            throw new BusinessException(
                    ErrorCode.INVALID_ALLOWANCE_STATUS_TRANSITION
            );
        }

        if (action == AllowanceRequestAction.APPROVE) {
            transferAllowance(
                    memberId,
                    current
            );
        }

        AllowanceRequestStatus nextStatus =
                getNextStatus(action);

        LocalDateTime updatedAt = LocalDateTime.now();
        int updatedCount =
                allowanceRequestMapper.updateAllowanceRequestStatus(
                        allowanceRequestId,
                        nextStatus,
                        updatedAt
                );

        if (updatedCount != 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_ALLOWANCE_STATUS_TRANSITION
            );
        }

        insertStatusNotification(
                allowanceRequestId,
                current.getChildId(),
                current.getRequestedAmount(),
                nextStatus,
                updatedAt
        );

        AllowanceRequestDetailRow updated =
                allowanceRequestMapper.findAllowanceRequestDetail(
                        allowanceRequestId
                );

        if (updated == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        return AllowanceRequestDetailResponse.from(updated);
    }

    private void insertStatusNotification(
            Long allowanceRequestId,
            Long childId,
            BigDecimal requestedAmount,
            AllowanceRequestStatus status,
            LocalDateTime createdAt
    ) {
        if (status != AllowanceRequestStatus.APPROVED
                && status != AllowanceRequestStatus.REJECTED) {
            return;
        }

        boolean approved = status == AllowanceRequestStatus.APPROVED;
        String title = approved
                ? "용돈 요청이 승인되었어요"
                : "용돈 요청이 거절되었어요";
        String content = approved
                ? String.format(
                        Locale.KOREA,
                        "%,.0f원 요청이 승인되었어요.",
                        requestedAmount
                )
                : String.format(
                        Locale.KOREA,
                        "%,.0f원 요청이 거절되었어요.",
                        requestedAmount
                );
        String notificationType = approved
                ? "ALLOWANCE_APPROVED"
                : "ALLOWANCE_REJECTED";

        allowanceRequestMapper.insertAllowanceStatusNotification(
                allowanceRequestId,
                childId,
                notificationType,
                title,
                content,
                createdAt
        );
    }

    private AllowanceRequestAction parseAllowanceRequestAction(
            UpdateAllowanceRequestStatus request
    ) {
        if (request == null
                || request.getAction() == null
                || request.getAction().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_ALLOWANCE_ACTION
            );
        }

        try {
            AllowanceRequestAction action = AllowanceRequestAction.valueOf(
                    request.getAction()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

            return action;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_ALLOWANCE_ACTION
            );
        }
    }

    private void validateTransferAccounts(
            UpdateAllowanceRequestStatus request,
            AllowanceRequestAction action
    ) {
        if (action == AllowanceRequestAction.APPROVE) {
            if (request.getSourceAccountId() == null
                    || request.getSourceAccountId() <= 0
                    || request.getDestinationAccountId() == null
                    || request.getDestinationAccountId() <= 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_ALLOWANCE_ACTION
                );
            }
            return;
        }

        if (request.getSourceAccountId() != null
                || request.getDestinationAccountId() != null) {
            throw new BusinessException(
                    ErrorCode.INVALID_ALLOWANCE_ACTION
            );
        }
    }

    private void transferAllowance(
            Long memberId,
            AllowanceRequestDetailRow allowanceRequest
    ) {
        Long sourceAccountId =
                allowanceRequestMapper
                        .findPrimaryParentDemandDepositAccountId(memberId);
        Long destinationAccountId =
                allowanceRequestMapper
                        .findPrimaryChildDemandDepositAccountId(
                                allowanceRequest.getChildId()
                        );

        if (sourceAccountId == null || destinationAccountId == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        TransferCreateResponse transferResponse = transferService.createTransfer(
                memberId,
                createAllowanceTransferIdempotencyKey(
                        allowanceRequest.getAllowanceRequestId()
                ),
                new CreateTransferRequest(
                        sourceAccountId,
                        destinationAccountId,
                        allowanceRequest.getRequestedAmount(),
                        ALLOWANCE_APPROVAL_TRANSFER_MEMO
                )
        );

        if (allowanceRequestMapper.linkAllowanceTransfer(
                transferResponse.getFinancialTransferId(),
                allowanceRequest.getAllowanceRequestId(),
                memberId
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.TRANSFER_PROCESSING_FAILED
            );
        }
    }

    private void transferAllowance(
            Long memberId,
            AllowanceRequestDetailRow allowanceRequest,
            UpdateAllowanceRequestStatus request
    ) {
        if (allowanceRequestMapper.countAllowanceDestinationAccount(
                allowanceRequest.getChildId(),
                request.getDestinationAccountId()
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_TRANSFER_REQUEST
            );
        }

        CreateTransferRequest transferRequest =
                new CreateTransferRequest(
                        request.getSourceAccountId(),
                        request.getDestinationAccountId(),
                        allowanceRequest.getRequestedAmount(),
                        ALLOWANCE_APPROVAL_TRANSFER_MEMO
                );

        TransferCreateResponse transferResponse =
                transferService.createTransfer(
                        memberId,
                        createAllowanceTransferIdempotencyKey(
                                allowanceRequest.getAllowanceRequestId()
                        ),
                        transferRequest
                );

        if (allowanceRequestMapper.linkAllowanceTransfer(
                transferResponse.getFinancialTransferId(),
                allowanceRequest.getAllowanceRequestId(),
                memberId
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.TRANSFER_PROCESSING_FAILED
            );
        }
    }

    private String createAllowanceTransferIdempotencyKey(
            Long allowanceRequestId
    ) {
        return UUID.nameUUIDFromBytes(
                ("ALLOWANCE_REQUEST:" + allowanceRequestId)
                        .getBytes(StandardCharsets.UTF_8)
        ).toString();
    }

    private void validateStatusChangeAccess(
            Long memberId,
            Long childId,
            AllowanceRequestAction action
    ) {
        boolean hasAccess;

        if (action == AllowanceRequestAction.CANCEL) {
            hasAccess =
                    allowanceRequestMapper
                            .countAllowanceRequestChildAccess(
                                    memberId,
                                    childId
                            ) > 0;
        } else {
            hasAccess =
                    allowanceRequestMapper
                            .countAllowanceRequestParentAccess(
                                    memberId,
                                    childId
                            ) > 0;
        }

        if (!hasAccess) {
            throw new BusinessException(
                    ErrorCode.ALLOWANCE_REQUEST_ACCESS_DENIED
            );
        }
    }

    private AllowanceRequestStatus getNextStatus(
            AllowanceRequestAction action
    ) {
        switch (action) {
            case APPROVE:
                return AllowanceRequestStatus.APPROVED;
            case REJECT:
                return AllowanceRequestStatus.REJECTED;
            case CANCEL:
                return AllowanceRequestStatus.CANCELED;
            default:
                throw new BusinessException(
                        ErrorCode.INVALID_ALLOWANCE_ACTION
                );
        }
    }
}
