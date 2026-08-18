package com.azas.domain.allowance.service;

import com.azas.domain.allowance.dto.AllowanceRequestListQuery;
import com.azas.domain.allowance.dto.AllowanceRequestListResponse;
import com.azas.domain.allowance.dto.AllowanceRequestListRow;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.azas.domain.allowance.mapper.AllowanceRequestMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllowanceRequestListServiceTest {

    private static final long MEMBER_ID = 20L;
    private static final long CHILD_ID = 6L;

    @Mock
    private AllowanceRequestMapper allowanceRequestMapper;

    @InjectMocks
    private AllowanceRequestServiceImpl allowanceRequestService;

    @Test
    void getsAllowanceRequestsWithCursorPagination() {
        when(allowanceRequestMapper.findActiveChildIdById(CHILD_ID))
                .thenReturn(CHILD_ID);
        when(allowanceRequestMapper.countAllowanceRequestAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);

        when(allowanceRequestMapper.findAllowanceRequests(
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(List.of(
                row(43L),
                row(42L),
                row(41L)
        ));

        AllowanceRequestListResponse response =
                allowanceRequestService.getAllowanceRequests(
                        MEMBER_ID,
                        CHILD_ID,
                        "PENDING",
                        null,
                        "2"
                );

        assertEquals(2, response.getItems().size());
        assertTrue(response.isHasNext());
        assertEquals(42L, response.getNextCursor());

        ArgumentCaptor<AllowanceRequestListQuery> captor =
                ArgumentCaptor.forClass(
                        AllowanceRequestListQuery.class
                );

        verify(allowanceRequestMapper)
                .findAllowanceRequests(captor.capture());

        assertEquals(CHILD_ID, captor.getValue().getChildId());
        assertEquals(
                AllowanceRequestStatus.PENDING,
                captor.getValue().getStatus()
        );
        assertEquals(3, captor.getValue().getLimit());
    }

    @Test
    void returnsEmptyPage() {
        when(allowanceRequestMapper.findActiveChildIdById(CHILD_ID))
                .thenReturn(CHILD_ID);
        when(allowanceRequestMapper.countAllowanceRequestAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(allowanceRequestMapper.findAllowanceRequests(
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(List.of());

        AllowanceRequestListResponse response =
                allowanceRequestService.getAllowanceRequests(
                        MEMBER_ID,
                        CHILD_ID,
                        null,
                        null,
                        null
                );

        assertTrue(response.getItems().isEmpty());
        assertFalse(response.isHasNext());
        assertEquals(null, response.getNextCursor());
    }

    @Test
    void rejectsInvalidStatus() {
        when(allowanceRequestMapper.findActiveChildIdById(CHILD_ID))
                .thenReturn(CHILD_ID);
        when(allowanceRequestMapper.countAllowanceRequestAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService.getAllowanceRequests(
                        MEMBER_ID,
                        CHILD_ID,
                        "WAITING",
                        null,
                        null
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsInvalidPageSize() {
        when(allowanceRequestMapper.findActiveChildIdById(CHILD_ID))
                .thenReturn(CHILD_ID);
        when(allowanceRequestMapper.countAllowanceRequestAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService.getAllowanceRequests(
                        MEMBER_ID,
                        CHILD_ID,
                        null,
                        null,
                        "101"
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsMissingChild() {
        when(allowanceRequestMapper.findActiveChildIdById(CHILD_ID))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService.getAllowanceRequests(
                        MEMBER_ID,
                        CHILD_ID,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                ErrorCode.CHILD_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(allowanceRequestMapper, never())
                .findAllowanceRequests(
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void rejectsMemberWithoutChildAccess() {
        when(allowanceRequestMapper.findActiveChildIdById(CHILD_ID))
                .thenReturn(CHILD_ID);
        when(allowanceRequestMapper.countAllowanceRequestAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> allowanceRequestService.getAllowanceRequests(
                        MEMBER_ID,
                        CHILD_ID,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    private AllowanceRequestListRow row(long requestId) {
        return new AllowanceRequestListRow(
                requestId,
                CHILD_ID,
                new BigDecimal("10000"),
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