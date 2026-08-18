package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.AllowanceRequestDetailResponse;
import com.azas.domain.allowance.dto.AllowanceRequestDetailRow;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.azas.domain.allowance.mapper.AllowanceRequestMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllowanceRequestDetailServiceTest {

    private static final long MEMBER_ID = 20L;
    private static final long CHILD_ID = 6L;
    private static final long ALLOWANCE_REQUEST_ID = 41L;

    @Mock
    private AllowanceRequestMapper allowanceRequestMapper;

    @InjectMocks
    private AllowanceRequestServiceImpl allowanceRequestService;

    @Test
    void getsAllowanceRequestDetail() {
        when(allowanceRequestMapper.findAllowanceRequestDetail(
                ALLOWANCE_REQUEST_ID
        )).thenReturn(detailRow());

        when(allowanceRequestMapper.countAllowanceRequestAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);

        AllowanceRequestDetailResponse response =
                allowanceRequestService.getAllowanceRequestDetail(
                        MEMBER_ID,
                        ALLOWANCE_REQUEST_ID
                );

        assertEquals(
                ALLOWANCE_REQUEST_ID,
                response.getAllowanceRequestId()
        );
        assertEquals(CHILD_ID, response.getChildId());
        assertEquals(
                new BigDecimal("10000"),
                response.getRequestedAmount()
        );
        assertEquals("밥먹을래", response.getMessage());
        assertEquals(
                AllowanceRequestStatus.PENDING,
                response.getStatus()
        );
        assertEquals(
                LocalDateTime.of(2026, 7, 15, 10, 30),
                response.getRequestedAt()
        );
    }

    @Test
    void rejectsInvalidAllowanceRequestId() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService
                        .getAllowanceRequestDetail(
                                MEMBER_ID,
                                0L
                        )
        );

        assertEquals(
                ErrorCode.BADREQUEST,
                exception.getErrorCode()
        );

        verify(allowanceRequestMapper, never())
                .findAllowanceRequestDetail(
                        org.mockito.ArgumentMatchers.anyLong()
                );
    }

    @Test
    void returnsNotFoundWhenRequestDoesNotExist() {
        when(allowanceRequestMapper.findAllowanceRequestDetail(
                ALLOWANCE_REQUEST_ID
        )).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService
                        .getAllowanceRequestDetail(
                                MEMBER_ID,
                                ALLOWANCE_REQUEST_ID
                        )
        );

        assertEquals(
                ErrorCode.ALLOWANCE_REQUEST_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(allowanceRequestMapper, never())
                .countAllowanceRequestAccess(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyLong()
                );
    }

    @Test
    void rejectsMemberWithoutChildAccess() {
        when(allowanceRequestMapper.findAllowanceRequestDetail(
                ALLOWANCE_REQUEST_ID
        )).thenReturn(detailRow());

        when(allowanceRequestMapper.countAllowanceRequestAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService
                        .getAllowanceRequestDetail(
                                MEMBER_ID,
                                ALLOWANCE_REQUEST_ID
                        )
        );

        assertEquals(
                ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    private AllowanceRequestDetailRow detailRow() {
        return new AllowanceRequestDetailRow(
                ALLOWANCE_REQUEST_ID,
                CHILD_ID,
                new BigDecimal("10000"),
                "밥먹을래",
                AllowanceRequestStatus.PENDING,
                LocalDateTime.of(
                        2026,
                        7,
                        15,
                        10,
                        30
                )
        );
    }
}