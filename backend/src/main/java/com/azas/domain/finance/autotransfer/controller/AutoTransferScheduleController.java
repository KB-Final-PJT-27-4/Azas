package com.azas.domain.finance.autotransfer.controller;

import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleDetailResponse;
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
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleListResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Api(tags = "자동이체")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AutoTransferScheduleController {

    private final AutoTransferScheduleService service;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation("자동이체 일정 등록")
    @PostMapping("/auto-transfer-schedules")
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

    @ApiOperation("자녀 자동이체 일정 목록 조회")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자동이체 일정 목록 조회 성공",
                    response = AutoTransferScheduleListResponse.class
            ),
            @ApiResponse(code = 400, message = "조회 조건 오류"),
            @ApiResponse(code = 403, message = "자녀 접근 권한 없음")
    })
    @GetMapping(
            "/children/{child_id}/auto-transfer-schedules"
    )
    public ResponseEntity<AutoTransferScheduleListResponse>
    getSchedules(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @PathVariable("child_id") Long childId,
            @RequestParam(
                    value = "status",
                    required = false
            ) String status,
            @RequestParam(
                    value = "cursor",
                    required = false
            ) String cursor,
            @RequestParam(
                    value = "size",
                    required = false
            ) Integer size
    ) {
        Long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        return ResponseEntity.ok(
                service.getSchedules(
                        memberId,
                        childId,
                        status,
                        cursor,
                        size
                )
        );
    }
    @ApiOperation("자동이체 일정 상세 조회")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자동이체 일정 상세 조회 성공",
                    response = AutoTransferScheduleDetailResponse.class
            ),
            @ApiResponse(code = 400, message = "일정 ID 오류"),
            @ApiResponse(code = 401, message = "인증 오류"),
            @ApiResponse(code = 403, message = "자녀 접근 권한 없음"),
            @ApiResponse(code = 404, message = "자동이체 일정 없음")
    })
    @GetMapping("/auto-transfer-schedules/{schedule_id}")
    public ResponseEntity<AutoTransferScheduleDetailResponse>
    getScheduleDetail(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @PathVariable("schedule_id") Long scheduleId
    ) {
        Long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        return ResponseEntity.ok(
                service.getScheduleDetail(
                        memberId,
                        scheduleId
                )
        );
    }
}