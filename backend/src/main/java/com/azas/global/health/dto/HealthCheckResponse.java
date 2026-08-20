package com.azas.global.health.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "애플리케이션 헬스 체크 응답")
public final class HealthCheckResponse {

    private static final String UP = "UP";

    private final String status;

    private HealthCheckResponse(String status) {
        this.status = status;
    }

    public static HealthCheckResponse up() {
        return new HealthCheckResponse(UP);
    }

    @ApiModelProperty(
            value = "애플리케이션 상태",
            example = "UP",
            required = true
    )
    public String getStatus() {
        return status;
    }
}
