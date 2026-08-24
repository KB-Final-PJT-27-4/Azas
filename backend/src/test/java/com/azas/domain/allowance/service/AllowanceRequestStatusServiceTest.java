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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
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

    @Mock
    private AllowanceRequestMapper allowanceRequestMapper;

    @Mock
    private TransferService transferService;

    @InjectMocks
    private AllowanceRequestServiceImpl allowanceRequestService;

    @Test
    void parentApprovesPendingRequestByTransferringBetweenPrimaryAccounts() {
        stubPendingRequestForParent();
        when(allowanceRequestMapper.findAllowanceRequestDetail(REQUEST_ID))
                .thenReturn(row(AllowanceRequestStatus.APPROVED));
        when(allowanceRequestMapper.updateAllowanceRequestStatus(
                eq(REQUEST_ID),
                eq(AllowanceRequestStatus.APPROVED),
                any(LocalDateTime.class)
        )).thenReturn(1);

        AllowanceRequestDetailResponse response =
                allowanceRequestService.updateAllowanceRequestStatus(
                        PARENT_ID,
                        REQUEST_ID,
                        request("APPROVE")
                );

        assertEquals(AllowanceRequestStatus.APPROVED, response.getStatus());
        verify(transferService).createTransfer(
                eq(PARENT_ID),
                any(String.class),
                org.mockito.ArgumentMatchers.argThat(transfer ->
                        transfer.getSourceAccountId().equals(SOURCE_ACCOUNT_ID)
                                && transfer.getDestinationAccountId().equals(
                                DESTINATION_ACCOUNT_ID
                        )
                                && transfer.getAmount().compareTo(
                                new BigDecimal("10000")
                        ) == 0
                )
        );
    }

    @Test
    void parentRejectsPendingRequestWithoutTransfer() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));
        when(allowanceRequestMapper.countAllowanceRequestParentAccess(
                PARENT_ID, CHILD_ID
        )).thenReturn(1);
        when(allowanceRequestMapper.findAllowanceRequestDetail(REQUEST_ID))
                .thenReturn(row(AllowanceRequestStatus.REJECTED));
        when(allowanceRequestMapper.updateAllowanceRequestStatus(
                eq(REQUEST_ID),
                eq(AllowanceRequestStatus.REJECTED),
                any(LocalDateTime.class)
        )).thenReturn(1);

        AllowanceRequestDetailResponse response =
                allowanceRequestService.updateAllowanceRequestStatus(
                        PARENT_ID,
                        REQUEST_ID,
                        request("REJECT")
                );

        assertEquals(AllowanceRequestStatus.REJECTED, response.getStatus());
        verify(transferService, never()).createTransfer(any(), any(), any());
    }

    @Test
    void childCancelsOwnPendingRequest() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));
        when(allowanceRequestMapper.countAllowanceRequestChildAccess(
                CHILD_MEMBER_ID, CHILD_ID
        )).thenReturn(1);
        when(allowanceRequestMapper.findAllowanceRequestDetail(REQUEST_ID))
                .thenReturn(row(AllowanceRequestStatus.CANCELED));
        when(allowanceRequestMapper.updateAllowanceRequestStatus(
                eq(REQUEST_ID),
                eq(AllowanceRequestStatus.CANCELED),
                any(LocalDateTime.class)
        )).thenReturn(1);

        AllowanceRequestDetailResponse response =
                allowanceRequestService.updateAllowanceRequestStatus(
                        CHILD_MEMBER_ID,
                        REQUEST_ID,
                        request("CANCEL")
                );

        assertEquals(AllowanceRequestStatus.CANCELED, response.getStatus());
        verify(transferService, never()).createTransfer(any(), any(), any());
    }

    @Test
    void rejectsApprovalWhenPrimaryTransferAccountIsMissing() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));
        when(allowanceRequestMapper.countAllowanceRequestParentAccess(
                PARENT_ID, CHILD_ID
        )).thenReturn(1);
        when(allowanceRequestMapper.findPrimaryParentDemandDepositAccountId(
                PARENT_ID
        )).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService.updateAllowanceRequestStatus(
                        PARENT_ID,
                        REQUEST_ID,
                        request("APPROVE")
                )
        );

        assertEquals(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                exception.getErrorCode());
        verify(transferService, never()).createTransfer(any(), any(), any());
    }

    @Test
    void doesNotApproveWhenTransferFails() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));
        when(allowanceRequestMapper.countAllowanceRequestParentAccess(
                PARENT_ID, CHILD_ID
        )).thenReturn(1);
        when(allowanceRequestMapper.findPrimaryParentDemandDepositAccountId(
                PARENT_ID
        )).thenReturn(SOURCE_ACCOUNT_ID);
        when(allowanceRequestMapper.findPrimaryChildDemandDepositAccountId(
                CHILD_ID
        )).thenReturn(DESTINATION_ACCOUNT_ID);
        BusinessException transferFailure = new BusinessException(
                ErrorCode.INSUFFICIENT_ACCOUNT_BALANCE
        );
        org.mockito.Mockito.doThrow(transferFailure)
                .when(transferService)
                .createTransfer(eq(PARENT_ID), any(String.class),
                        any(CreateTransferRequest.class));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService.updateAllowanceRequestStatus(
                        PARENT_ID,
                        REQUEST_ID,
                        request("APPROVE")
                )
        );

        assertEquals(ErrorCode.INSUFFICIENT_ACCOUNT_BALANCE,
                exception.getErrorCode());
        verify(allowanceRequestMapper, never()).updateAllowanceRequestStatus(
                any(), any(), any());
    }

    private void stubPendingRequestForParent() {
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(
                REQUEST_ID
        )).thenReturn(row(AllowanceRequestStatus.PENDING));
        when(allowanceRequestMapper.countAllowanceRequestParentAccess(
                PARENT_ID, CHILD_ID
        )).thenReturn(1);
        when(allowanceRequestMapper.findPrimaryParentDemandDepositAccountId(
                PARENT_ID
        )).thenReturn(SOURCE_ACCOUNT_ID);
        when(allowanceRequestMapper.findPrimaryChildDemandDepositAccountId(
                CHILD_ID
        )).thenReturn(DESTINATION_ACCOUNT_ID);
        TransferCreateResponse transferResponse = mock(
                TransferCreateResponse.class
        );
        when(transferResponse.getFinancialTransferId()).thenReturn(301L);
        when(transferService.createTransfer(
                eq(PARENT_ID), any(String.class),
                any(CreateTransferRequest.class)
        )).thenReturn(transferResponse);
        when(allowanceRequestMapper.linkAllowanceTransfer(
                301L, REQUEST_ID, PARENT_ID
        )).thenReturn(1);
    }

    private UpdateAllowanceRequestStatus request(String action) {
        return new UpdateAllowanceRequestStatus(action, null, null);
    }

    private AllowanceRequestDetailRow row(AllowanceRequestStatus status) {
        return new AllowanceRequestDetailRow(
                REQUEST_ID,
                CHILD_ID,
                new BigDecimal("10000"),
                "allowance request",
                status,
                LocalDateTime.of(2026, 7, 15, 10, 30)
        );
    }
}
