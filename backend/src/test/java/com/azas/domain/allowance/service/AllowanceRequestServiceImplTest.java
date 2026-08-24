package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.AllowanceRequestInsertCommand;
import com.azas.domain.allowance.dto.AllowanceRequestDetailRow;
import com.azas.domain.allowance.dto.AllowanceRequestResponse;
import com.azas.domain.allowance.dto.CreateAllowanceRequest;
import com.azas.domain.allowance.dto.UpdateAllowanceRequestStatus;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.azas.domain.allowance.mapper.AllowanceRequestMapper;
import com.azas.domain.child.service.ChildFeaturePermissionService;
import com.azas.domain.finance.transfer.dto.CreateTransferRequest;
import com.azas.domain.finance.transfer.dto.TransferCreateResponse;
import com.azas.domain.finance.transfer.service.TransferService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllowanceRequestServiceImplTest {

    private static final long MEMBER_ID = 20L;
    private static final long CHILD_ID = 10L;
    private static final long SOURCE_ACCOUNT_ID = 101L;
    private static final long DESTINATION_ACCOUNT_ID = 202L;

    @Mock
    private AllowanceRequestMapper allowanceRequestMapper;

    @Mock
    private ChildFeaturePermissionService childFeaturePermissionService;

    @Mock
    private TransferService transferService;

    @InjectMocks
    private AllowanceRequestServiceImpl allowanceRequestService;

    @Test
    void createsAllowanceRequestForLinkedChildMember() {
        CreateAllowanceRequest request = request(
                "10000",
                "  친구 생일 선물을 사려고 해요!  "
        );

        when(allowanceRequestMapper.findActiveChildIdByMemberId(MEMBER_ID))
                .thenReturn(CHILD_ID);
        doAnswer(invocation -> {
            AllowanceRequestInsertCommand command =
                    invocation.getArgument(0);
            ReflectionTestUtils.setField(
                    command,
                    "allowanceRequestId",
                    41L
            );
            return 1;
        }).when(allowanceRequestMapper)
                .insertAllowanceRequest(any());

        AllowanceRequestResponse response =
                allowanceRequestService.createAllowanceRequest(
                        MEMBER_ID,
                        request
                );

        assertEquals(41L, response.getAllowanceRequestId());
        assertEquals(CHILD_ID, response.getChildId());
        assertEquals(
                new BigDecimal("10000"),
                response.getRequestedAmount()
        );
        assertEquals(
                "친구 생일 선물을 사려고 해요!",
                response.getMessage()
        );
        assertEquals(AllowanceRequestStatus.PENDING, response.getStatus());
        assertNotNull(response.getRequestedAt());

        ArgumentCaptor<AllowanceRequestInsertCommand> captor =
                ArgumentCaptor.forClass(
                        AllowanceRequestInsertCommand.class
                );
        verify(allowanceRequestMapper)
                .insertAllowanceRequest(captor.capture());
        assertEquals(CHILD_ID, captor.getValue().getChildId());
        assertEquals(
                "친구 생일 선물을 사려고 해요!",
                captor.getValue().getMessage()
        );
        verify(allowanceRequestMapper)
                .insertAllowanceRequestedNotification(
                        eq(41L),
                        eq(CHILD_ID),
                        eq(new BigDecimal("10000")),
                        eq("친구 생일 선물을 사려고 해요!"),
                        any()
                );
    }

    @Test
    void notifiesChildWhenParentApprovesAllowanceRequest() {
        AllowanceRequestDetailRow pending = new AllowanceRequestDetailRow(
                41L,
                CHILD_ID,
                new BigDecimal("10000"),
                "친구 생일 선물을 사려고 해요!",
                AllowanceRequestStatus.PENDING,
                java.time.LocalDateTime.now()
        );
        AllowanceRequestDetailRow approved = new AllowanceRequestDetailRow(
                41L,
                CHILD_ID,
                new BigDecimal("10000"),
                "친구 생일 선물을 사려고 해요!",
                AllowanceRequestStatus.APPROVED,
                java.time.LocalDateTime.now()
        );
        when(allowanceRequestMapper.findAllowanceRequestDetailForUpdate(41L))
                .thenReturn(pending);
        when(allowanceRequestMapper.findAllowanceRequestDetail(41L))
                .thenReturn(approved);
        when(allowanceRequestMapper.countAllowanceRequestParentAccess(
                MEMBER_ID, CHILD_ID
        )).thenReturn(1);
        when(allowanceRequestMapper.findPrimaryParentDemandDepositAccountId(
                MEMBER_ID
        )).thenReturn(SOURCE_ACCOUNT_ID);
        when(allowanceRequestMapper.findPrimaryChildDemandDepositAccountId(
                CHILD_ID
        )).thenReturn(DESTINATION_ACCOUNT_ID);
        TransferCreateResponse transferResponse =
                mock(TransferCreateResponse.class);
        when(transferResponse.getFinancialTransferId()).thenReturn(51L);
        when(transferService.createTransfer(
                eq(MEMBER_ID),
                any(String.class),
                any(CreateTransferRequest.class)
        )).thenReturn(transferResponse);
        when(allowanceRequestMapper.linkAllowanceTransfer(
                51L,
                41L,
                MEMBER_ID
        )).thenReturn(1);
        when(allowanceRequestMapper.updateAllowanceRequestStatus(
                eq(41L), eq(AllowanceRequestStatus.APPROVED), any()
        )).thenReturn(1);

        allowanceRequestService.updateAllowanceRequestStatus(
                MEMBER_ID,
                41L,
                updateRequest("APPROVE")
        );

        verify(allowanceRequestMapper)
                .insertAllowanceStatusNotification(
                        eq(41L),
                        eq(CHILD_ID),
                        eq("ALLOWANCE_APPROVED"),
                        eq("용돈 요청이 승인되었어요"),
                        eq("10,000원 요청이 승인되었어요."),
                        any()
                );
    }

    @Test
    void rejectsMemberWithoutLinkedActiveChildProfile() {
        when(allowanceRequestMapper.findActiveChildIdByMemberId(MEMBER_ID))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService.createAllowanceRequest(
                        MEMBER_ID,
                        request("10000", "영화를 보고 싶어요.")
                )
        );

        assertEquals(
                ErrorCode.CHILD_MEMBER_ACCESS_REQUIRED,
                exception.getErrorCode()
        );
        verify(allowanceRequestMapper, never())
                .insertAllowanceRequest(any());
    }

    @Test
    void rejectsAllowanceRequestWhenPermissionIsDisabled() {
        when(allowanceRequestMapper.findActiveChildIdByMemberId(MEMBER_ID))
                .thenReturn(CHILD_ID);
        doThrow(new BusinessException(
                ErrorCode.ALLOWANCE_REQUEST_DISABLED
        )).when(childFeaturePermissionService)
                .validateAllowanceRequestEnabled(CHILD_ID);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService.createAllowanceRequest(
                        MEMBER_ID,
                        request("10000", "용돈을 요청합니다.")
                )
        );

        assertEquals(
                ErrorCode.ALLOWANCE_REQUEST_DISABLED,
                exception.getErrorCode()
        );
        verify(allowanceRequestMapper, never())
                .insertAllowanceRequest(any());
    }

    @Test
    void throwsInternalServerErrorWhenInsertFails() {
        when(allowanceRequestMapper.findActiveChildIdByMemberId(MEMBER_ID))
                .thenReturn(CHILD_ID);
        when(allowanceRequestMapper.insertAllowanceRequest(any()))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService.createAllowanceRequest(
                        MEMBER_ID,
                        request("10000", "영화를 보고 싶어요.")
                )
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    @Test
    void allowsMultipleRequestsWithoutMonthlyLimit() {
        when(allowanceRequestMapper.findActiveChildIdByMemberId(MEMBER_ID))
                .thenReturn(CHILD_ID);
        doAnswer(invocation -> {
            AllowanceRequestInsertCommand command =
                    invocation.getArgument(0);
            ReflectionTestUtils.setField(
                    command,
                    "allowanceRequestId",
                    41L
            );
            return 1;
        }).when(allowanceRequestMapper)
                .insertAllowanceRequest(any());

        allowanceRequestService.createAllowanceRequest(
                MEMBER_ID,
                request("10000", "첫 번째 요청이에요.")
        );
        allowanceRequestService.createAllowanceRequest(
                MEMBER_ID,
                request("20000", "두 번째 요청이에요.")
        );

        verify(allowanceRequestMapper,
                org.mockito.Mockito.times(2))
                .insertAllowanceRequest(any());
    }

    private CreateAllowanceRequest request(
            String amount,
            String message
    ) {
        CreateAllowanceRequest request = new CreateAllowanceRequest();
        ReflectionTestUtils.setField(
                request,
                "requestedAmount",
                new BigDecimal(amount)
        );
        ReflectionTestUtils.setField(request, "message", message);
        return request;
    }

    private UpdateAllowanceRequestStatus updateRequest(String action) {
        return new UpdateAllowanceRequestStatus(
                action,
                SOURCE_ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID
        );
    }
}
