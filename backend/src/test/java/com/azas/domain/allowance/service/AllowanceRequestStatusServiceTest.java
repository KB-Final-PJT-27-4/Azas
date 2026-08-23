package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.AllowanceRequestDetailResponse;
import com.azas.domain.allowance.dto.AllowanceRequestDetailRow;
import com.azas.domain.allowance.dto.UpdateAllowanceRequestStatus;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.azas.domain.allowance.mapper.AllowanceRequestMapper;
import com.azas.domain.finance.transfer.dto.CreateTransferRequest;
import com.azas.domain.finance.transfer.dto.TransferCreateResponse;
import com.azas.domain.finance.transfer.service.TransferService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllowanceRequestStatusServiceTest {

    private static final long PARENT_ID = 20L;
    private static final long CHILD_MEMBER_ID = 30L;
    private static final long CHILD_ID = 6L;
    private static final long REQUEST_ID = 41L;
    private static final long SOURCE_ACCOUNT_ID = 101L;
    private static final long DESTINATION_ACCOUNT_ID = 202L;
    private static final long TRANSFER_ID = 303L;

    @Mock
    private AllowanceRequestMapper allowanceRequestMapper;

    @Mock
    private TransferService transferService;

    @InjectMocks
    private AllowanceRequestServiceImpl allowanceRequestService;

    @Test
    void parentApprovesPendingRequest() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));

        when(allowanceRequestMapper.findAllowanceRequestDetail(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.APPROVED));

        when(allowanceRequestMapper
                .countAllowanceRequestParentAccess(
                        PARENT_ID,
                        CHILD_ID
                ))
                .thenReturn(1);

        when(allowanceRequestMapper.countAllowanceDestinationAccount(
                CHILD_ID,
                DESTINATION_ACCOUNT_ID
        )).thenReturn(1);

        TransferCreateResponse transferResponse =
                mock(TransferCreateResponse.class);
        when(transferResponse.getFinancialTransferId())
                .thenReturn(TRANSFER_ID);
        when(transferService.createTransfer(
                eq(PARENT_ID),
                any(String.class),
                any(CreateTransferRequest.class)
        )).thenReturn(transferResponse);

        when(allowanceRequestMapper.linkAllowanceTransfer(
                TRANSFER_ID,
                REQUEST_ID,
                PARENT_ID
        )).thenReturn(1);

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

        verify(transferService).createTransfer(
                eq(PARENT_ID),
                any(String.class),
                org.mockito.ArgumentMatchers.argThat(transfer ->
                        transfer.getSourceAccountId().equals(
                                SOURCE_ACCOUNT_ID
                        )
                                && transfer.getDestinationAccountId().equals(
                                DESTINATION_ACCOUNT_ID
                        )
                                && transfer.getAmount().compareTo(
                                new BigDecimal("10000")
                        ) == 0
                )
        );
        verify(allowanceRequestMapper).linkAllowanceTransfer(
                TRANSFER_ID,
                REQUEST_ID,
                PARENT_ID
        );
    }

    @Test
    void parentRejectsPendingRequest() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));

        when(allowanceRequestMapper.findAllowanceRequestDetail(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.REJECTED));

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

        verify(allowanceRequestMapper)
                .insertAllowanceStatusNotification(
                        eq(REQUEST_ID),
                        eq(CHILD_ID),
                        eq("ALLOWANCE_REJECTED"),
                        eq("용돈 요청이 거절되었어요"),
                        eq("10,000원 요청이 거절되었어요."),
                        any(LocalDateTime.class)
                );
        verify(transferService, never()).createTransfer(
                any(),
                any(),
                any()
        );
    }

    @Test
    void childCancelsOwnPendingRequest() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));

        when(allowanceRequestMapper.findAllowanceRequestDetail(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.CANCELED));

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

        verify(allowanceRequestMapper, never())
                .insertAllowanceStatusNotification(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
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
                .findAllowanceRequestDetailForUpdate(
                        org.mockito.ArgumentMatchers.anyLong()
                );
    }

    @Test
    void rejectsParentCancelAction() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
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
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
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
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
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
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));

        when(allowanceRequestMapper
                .countAllowanceRequestParentAccess(
                        PARENT_ID,
                        CHILD_ID
                ))
                .thenReturn(1);

        stubSuccessfulTransfer();

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

    @Test
    void rejectsApprovalWithoutTransferAccounts() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService
                        .updateAllowanceRequestStatus(
                                PARENT_ID,
                                REQUEST_ID,
                                new UpdateAllowanceRequestStatus(
                                        "APPROVE",
                                        null,
                                        null
                                )
                        )
        );

        assertEquals(
                ErrorCode.INVALID_ALLOWANCE_ACTION,
                exception.getErrorCode()
        );
        verify(allowanceRequestMapper, never())
                .findAllowanceRequestDetailForUpdate(any());
    }

    @Test
    void rejectsDestinationAccountOwnedByAnotherChild() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));
        when(allowanceRequestMapper.countAllowanceRequestParentAccess(
                PARENT_ID,
                CHILD_ID
        )).thenReturn(1);
        when(allowanceRequestMapper.countAllowanceDestinationAccount(
                CHILD_ID,
                DESTINATION_ACCOUNT_ID
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
                ErrorCode.INVALID_TRANSFER_REQUEST,
                exception.getErrorCode()
        );
        verify(transferService, never()).createTransfer(
                any(),
                any(),
                any()
        );
        verify(allowanceRequestMapper, never())
                .updateAllowanceRequestStatus(any(), any(), any());
    }

    @Test
    void doesNotApproveWhenTransferFails() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));
        when(allowanceRequestMapper.countAllowanceRequestParentAccess(
                PARENT_ID,
                CHILD_ID
        )).thenReturn(1);
        when(allowanceRequestMapper.countAllowanceDestinationAccount(
                CHILD_ID,
                DESTINATION_ACCOUNT_ID
        )).thenReturn(1);
        doThrow(new BusinessException(
                ErrorCode.INSUFFICIENT_ACCOUNT_BALANCE
        )).when(transferService).createTransfer(
                eq(PARENT_ID),
                any(String.class),
                any(CreateTransferRequest.class)
        );

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
                ErrorCode.INSUFFICIENT_ACCOUNT_BALANCE,
                exception.getErrorCode()
        );
        verify(allowanceRequestMapper, never())
                .updateAllowanceRequestStatus(any(), any(), any());
    }

    private UpdateAllowanceRequestStatus request(String action) {
        boolean approve = "APPROVE".equals(action);
        return new UpdateAllowanceRequestStatus(
                action,
                approve ? SOURCE_ACCOUNT_ID : null,
                approve ? DESTINATION_ACCOUNT_ID : null
        );
    }

    private void stubSuccessfulTransfer() {
        when(allowanceRequestMapper.countAllowanceDestinationAccount(
                CHILD_ID,
                DESTINATION_ACCOUNT_ID
        )).thenReturn(1);

        TransferCreateResponse transferResponse =
                mock(TransferCreateResponse.class);
        when(transferResponse.getFinancialTransferId())
                .thenReturn(TRANSFER_ID);
        when(transferService.createTransfer(
                eq(PARENT_ID),
                any(String.class),
                any(CreateTransferRequest.class)
        )).thenReturn(transferResponse);

        when(allowanceRequestMapper.linkAllowanceTransfer(
                TRANSFER_ID,
                REQUEST_ID,
                PARENT_ID
        )).thenReturn(1);
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
