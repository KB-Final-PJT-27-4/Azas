package com.azas.global.response;

public final class ApiErrorResponse {

    private final ErrorDetail error;

    private ApiErrorResponse(String code, String message) {
        this.error = new ErrorDetail(code, message);
    }

    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message);
    }

    public ErrorDetail getError() {
        return error;
    }

    public static final class ErrorDetail {

        private final String code;
        private final String message;

        private ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
