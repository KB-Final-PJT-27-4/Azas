package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.AllowanceRequestDetailResponse;
import com.azas.domain.allowance.dto.AllowanceRequestDetailRow;
import com.azas.domain.allowance.dto.UpdateAllowanceRequestStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllowanceRequestStatusServiceTest {

    private static final long PARENT_ID = 20L;
    private static final long CHILD_MEMBER_ID = 30L;
    private static final long CHILD_ID = 6L;
    private static final long REQUEST_ID = 41L;

    @Mock
    private AllowanceRequestMapper allowanceRequestMapper;

    @InjectMocks
    private AllowanceRequestServiceImpl allowanceRequestService;

    @Test
    void parentApprovesPendingRequest() {
        when(allowanceRequestMapper.findAllowanceRequestDetail(
                REQUEST_ID
        )).thenReturn(
                row(AllowanceRequestStatus.PENDING),
                row(AllowanceRequestStatus.APPROVED)
        );

        when(allowanceRequestMapper
                .countAllowanceRequestParentAccess(
                        PARENT_ID,
                        CHILD_ID
                ))
                .thenReturn(1);

        when(allowanceRequestMapper.updateAllowanceRequestStatus(
                org.mockito.ArgumentMatchers.eq(REQUEST_ID),
                org.mockito.ArgumentMatchers.eq(
                        AllowanceRequestStatus.APPROVED
                ),
                any(LocalDateTime.class)
        )).thenReturn(1);

        AllowanceRequestDetailResponse response =
                allowanceRequestService.updateAllowanceRequestStatus(
                        PARENT_ID,
                        REQUEST_ID,
                        request("APPROVE")
                );

        assertEquals(
                AllowanceRequestStatus.APPROVED,
                response.getStatus()
        );
    }

    @Test
    void parentRejectsPendingRequest() {
        when(allowanceRequestMapper.findAllowanceRequestDetail(
                REQUEST_ID
        )).thenReturn(
                row(AllowanceRequestStatus.PENDING),
                row(AllowanceRequestStatus.REJECTED)
        );

        when(allowanceRequestMapper
                .countAllowanceRequestParentAccess(
                        PARENT_ID,
                        CHILD_ID
                ))
                .thenReturn(1);

        when(allowanceRequestMapper.updateAllowanceRequestStatus(
                org.mockito.ArgumentMatchers.eq(REQUEST_ID),
                org.mockito.ArgumentMatchers.eq(
                        AllowanceRequestStatus.REJECTED
                ),
                any(LocalDateTime.class)
        )).thenReturn(1);

        AllowanceRequestDetailResponse response =
                allowanceRequestService.updateAllowanceRequestStatus(
                        PARENT_ID,
                        REQUEST_ID,
                        request("REJECT")
                );

        assertEquals(
                AllowanceRequestStatus.REJECTED,
                response.getStatus()
        );
    }

    @Test
    void childCancelsOwnPendingRequest() {
        when(allowanceRequestMapper.findAllowanceRequestDetail(
                REQUEST_ID
        )).thenReturn(
                row(AllowanceRequestStatus.PENDING),
                row(AllowanceRequestStatus.CANCELED)
        );

        when(allowanceRequestMapper
                .countAllowanceRequestChildAccess(
                        CHILD_MEMBER_ID,
                        CHILD_ID
                ))
                .thenReturn(1);

        when(allowanceRequestMapper.updateAllowanceRequestStatus(
                org.mockito.ArgumentMatchers.eq(REQUEST_ID),
                org.mockito.ArgumentMatchers.eq(
                        AllowanceRequestStatus.CANCELED
                ),
                any(LocalDateTime.class)
        )).thenReturn(1);

        AllowanceRequestDetailResponse response =
                allowanceRequestService.updateAllowanceRequestStatus(
                        CHILD_MEMBER_ID,
                        REQUEST_ID,
                        request("CANCEL")
                );

        assertEquals(
                AllowanceRequestStatus.CANCELED,
                response.getStatus()
        );
    }

    @Test
    void rejectsUnsupportedAction() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService
                        .updateAllowanceRequestStatus(
                                PARENT_ID,
                                REQUEST_ID,
                                request("ACCEPT")
                        )
        );

        assertEquals(
                ErrorCode.INVALID_ALLOWANCE_ACTION,
                exception.getErrorCode()
        );

        verify(allowanceRequestMapper, never())
                .findAllowanceRequestDetail(
                        org.mockito.ArgumentMatchers.anyLong()
                );
    }

    @Test
    void rejectsParentCancelAction() {
        when(allowanceRequestMapper.findAllowanceRequestDetail(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));

        when(allowanceRequestMapper
                .countAllowanceRequestChildAccess(
                        PARENT_ID,
                        CHILD_ID
                ))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService
                        .updateAllowanceRequestStatus(
                                PARENT_ID,
                                REQUEST_ID,
                                request("CANCEL")
                        )
        );

        assertEquals(
                ErrorCode.ALLOWANCE_REQUEST_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsChildApproveAction() {
        when(allowanceRequestMapper.findAllowanceRequestDetail(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));

        when(allowanceRequestMapper
                .countAllowanceRequestParentAccess(
                        CHILD_MEMBER_ID,
                        CHILD_ID
                ))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService
                        .updateAllowanceRequestStatus(
                                CHILD_MEMBER_ID,
                                REQUEST_ID,
                                request("APPROVE")
                        )
        );

        assertEquals(
                ErrorCode.ALLOWANCE_REQUEST_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsAlreadyProcessedRequest() {
        when(allowanceRequestMapper.findAllowanceRequestDetail(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.APPROVED));

        when(allowanceRequestMapper
                .countAllowanceRequestParentAccess(
                        PARENT_ID,
                        CHILD_ID
                ))
                .thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService
                        .updateAllowanceRequestStatus(
                                PARENT_ID,
                                REQUEST_ID,
                                request("REJECT")
                        )
        );

        assertEquals(
                ErrorCode.INVALID_ALLOWANCE_STATUS_TRANSITION,
                exception.getErrorCode()
        );

        verify(allowanceRequestMapper, never())
                .updateAllowanceRequestStatus(
                        org.mockito.ArgumentMatchers.anyLong(),
                        any(),
                        any()
                );
    }

    @Test
    void rejectsConcurrentStatusChange() {
        when(allowanceRequestMapper.findAllowanceRequestDetail(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));

        when(allowanceRequestMapper
                .countAllowanceRequestParentAccess(
                        PARENT_ID,
                        CHILD_ID
                ))
                .thenReturn(1);

        when(allowanceRequestMapper.updateAllowanceRequestStatus(
                org.mockito.ArgumentMatchers.eq(REQUEST_ID),
                org.mockito.ArgumentMatchers.eq(
                        AllowanceRequestStatus.APPROVED
                ),
                any(LocalDateTime.class)
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService
                        .updateAllowanceRequestStatus(
                                PARENT_ID,
                                REQUEST_ID,
                                request("APPROVE")
                        )
        );

        assertEquals(
                ErrorCode.INVALID_ALLOWANCE_STATUS_TRANSITION,
                exception.getErrorCode()
        );
    }

    private UpdateAllowanceRequestStatus request(String action) {
        UpdateAllowanceRequestStatus request =
                new UpdateAllowanceRequestStatus();

        org.springframework.test.util.ReflectionTestUtils
                .setField(request, "action", action);

        return request;
    }

    private AllowanceRequestDetailRow row(
            AllowanceRequestStatus status
    ) {
        return new AllowanceRequestDetailRow(
                REQUEST_ID,
                CHILD_ID,
                new BigDecimal("10000"),
                "밥먹을래",
                status,
                LocalDateTime.of(2026, 7, 15, 10, 30)
        );
    }
}