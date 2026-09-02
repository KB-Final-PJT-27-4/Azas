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
import org.springframework.web.bind.annotation.*;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleListResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.azas.domain.finance.autotransfer.dto.UpdateAutoTransferScheduleRequest;

import javax.validation.Valid;

@Api(tags = "자동이체")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AutoTransferScheduleController {

    private final AutoTransferScheduleService autoTransferScheduleService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation("자동이체 일정 등록")
    @ApiResponses({
            @ApiResponse(code = 201, message = "자동이체 일정 등록 성공"),
            @ApiResponse(code = 400, message = "요청 형식 또는 멱등성 키 오류"),
            @ApiResponse(code = 401, message = "인증 오류"),
            @ApiResponse(code = 403, message = "계좌 또는 자녀 접근 권한 없음"),
            @ApiResponse(code = 404, message = "계좌를 찾을 수 없음"),
            @ApiResponse(code = 409, message = "중복 일정"),
            @ApiResponse(code = 422, message = "일정 또는 계좌 조건 오류")
    })
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
                autoTransferScheduleService.createSchedule(
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
                autoTransferScheduleService.getSchedules(
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
                autoTransferScheduleService.getScheduleDetail(
                        memberId,
                        scheduleId
                )
        );
    }

    @ApiOperation("자동이체 일정 수정·일시정지·재개")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자동이체 일정 변경 성공",
                    response = AutoTransferScheduleDetailResponse.class
            ),
            @ApiResponse(code = 400, message = "요청 형식 오류"),
            @ApiResponse(code = 401, message = "인증 오류"),
            @ApiResponse(code = 403, message = "일정 변경 권한 없음"),
            @ApiResponse(code = 404, message = "자동이체 일정 없음"),
            @ApiResponse(code = 409, message = "중복 일정 또는 상태 전이 오류"),
            @ApiResponse(code = 422, message = "일정 조건 오류")
    })
    @PatchMapping("/auto-transfer-schedules/{schedule_id}")
    public ResponseEntity<AutoTransferScheduleDetailResponse>
    updateSchedule(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @PathVariable("schedule_id") Long scheduleId,
            @Valid @RequestBody
            UpdateAutoTransferScheduleRequest request
    ) {
        Long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        return ResponseEntity.ok(
                autoTransferScheduleService.updateSchedule(
                        memberId,
                        scheduleId,
                        request
                )
        );
    }

    @ApiOperation(
            value = "자동이체 일정 해지",
            notes = "자동이체 일정을 삭제하지 않고 CANCELED 상태로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(code = 204, message = "자동이체 일정 해지 성공"),
            @ApiResponse(code = 400, message = "잘못된 요청"),
            @ApiResponse(code = 401, message = "인증 실패"),
            @ApiResponse(code = 403, message = "일정 해지 권한 없음"),
            @ApiResponse(code = 404, message = "자동이체 일정 없음"),
            @ApiResponse(code = 409, message = "해지할 수 없는 일정 상태")
    })

    @DeleteMapping("/auto-transfer-schedules/{schedule_id}")
    public ResponseEntity<Void> cancelSchedule(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @PathVariable("schedule_id") Long scheduleId
    ) {
        Long memberId =
                accessTokenMemberResolver.resolveMemberId(authorization);

        autoTransferScheduleService.cancelSchedule(memberId, scheduleId);

        return ResponseEntity.noContent().build();
    }
}
