package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.AllowanceRequestInsertCommand;
import com.azas.domain.allowance.dto.AllowanceRequestResponse;
import com.azas.domain.allowance.dto.CreateAllowanceRequest;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.azas.domain.allowance.mapper.AllowanceRequestMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AllowanceRequestServiceImpl
        implements AllowanceRequestService {

    private final AllowanceRequestMapper allowanceRequestMapper;

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
}