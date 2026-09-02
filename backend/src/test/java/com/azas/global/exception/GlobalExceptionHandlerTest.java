package com.azas.global.exception;

import com.azas.global.response.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void returnsGenericErrorAndKeepsUnexpectedExceptionDetailsOutOfResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/v1/family-invitations/test-token"
        );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnexpectedException(
                        request,
                        new IllegalStateException("internal cause")
                );

        assertEquals(500, response.getStatusCodeValue());
        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                response.getBody().getError().getCode()
        );
        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                response.getBody().getError().getMessage()
        );
    }
}
