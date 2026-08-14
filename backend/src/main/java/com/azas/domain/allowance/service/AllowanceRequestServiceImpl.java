package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.*;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.azas.domain.allowance.mapper.AllowanceRequestMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllowanceRequestServiceImpl implements AllowanceRequestService {

    private final AllowanceRequestMapper allowanceRequestMapper;
    private static final int DEFAULT_LIST_SIZE = 20;
    private static final int MAX_LIST_SIZE = 100;

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
}