package com.azas.domain.finance.autotransfer.controller;

import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleResponse;
import com.azas.domain.finance.autotransfer.dto.CreateAutoTransferScheduleRequest;
import com.azas.domain.finance.autotransfer.service.AutoTransferScheduleService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "자동이체")
@RestController
@RequestMapping("/api/v1/auto-transfer-schedules")
@RequiredArgsConstructor
public class AutoTransferScheduleController {

    private final AutoTransferScheduleService service;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation("자동이체 일정 등록")
    @PostMapping
    public ResponseEntity<AutoTransferScheduleResponse> createSchedule(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @RequestHeader(
                    value = "Idempotency-Key",
                    required = false
            ) String idempotencyKey,
            @Valid @RequestBody
            CreateAutoTransferScheduleRequest request
    ) {
        Long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        AutoTransferScheduleResponse response =
                service.createSchedule(
                        memberId,
                        idempotencyKey,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}