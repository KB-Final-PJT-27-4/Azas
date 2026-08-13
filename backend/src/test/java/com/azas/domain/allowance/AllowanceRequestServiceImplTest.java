package com.azas.domain.allowance;

import com.azas.domain.allowance.dto.AllowanceRequestInsertCommand;
import com.azas.domain.allowance.dto.AllowanceRequestResponse;
import com.azas.domain.allowance.dto.CreateAllowanceRequest;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.azas.domain.allowance.mapper.AllowanceRequestMapper;
import com.azas.domain.allowance.service.AllowanceRequestServiceImpl;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllowanceRequestServiceImplTest {

    private static final long MEMBER_ID = 20L;
    private static final long CHILD_ID = 10L;

    @Mock
    private AllowanceRequestMapper allowanceRequestMapper;

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
}
