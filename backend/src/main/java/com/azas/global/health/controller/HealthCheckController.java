package com.azas.global.health.controller;

import com.azas.global.health.dto.HealthCheckResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "시스템")
@RestController
@RequestMapping("/api/v1")
public class HealthCheckController {

    @ApiOperation(
            value = "애플리케이션 헬스 체크",
            notes = "Tomcat과 Spring MVC 애플리케이션의 정상 기동 여부를 확인합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "애플리케이션 정상 기동",
                    response = HealthCheckResponse.class
            )
    })
    @GetMapping("/health")
    public ResponseEntity<HealthCheckResponse> health() {
        return ResponseEntity.ok(HealthCheckResponse.up());
    }
}
