package com.azas.global.exception;

import com.azas.global.response.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return createResponse(errorCode);
    }

    @ExceptionHandler({
            BindException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(Exception exception) {
        return createResponse(ErrorCode.BADREQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            HttpServletRequest request,
            Exception exception
    ) {
        log.error(
                "Unhandled API exception. method={}, uri={}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return createResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiErrorResponse> createResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiErrorResponse.of(errorCode.name(), errorCode.getMessage()));
    }
}
