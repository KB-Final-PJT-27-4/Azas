package com.azas.domain.finance.autotransfer.controller;

import com.azas.domain.finance.autotransfer.dto.AutoTransferRetryResponse;
import com.azas.domain.finance.autotransfer.service.AutoTransferRetryService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "자동이체")
@RestController
@RequestMapping("/api/v1/auto-transfer-schedules")
@RequiredArgsConstructor
public class AutoTransferRetryController {

    private final AutoTransferRetryService retryService;
    private final AccessTokenMemberResolver memberResolver;

    @ApiOperation(
            value = "자동이체 실패 회차 수동 재시도",
            notes = "가장 최근 실패한 자동이체 회차를 다시 실행합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "재시도 처리 완료",
                    response = AutoTransferRetryResponse.class
            ),
            @ApiResponse(code = 400, message = "요청 형식 오류"),
            @ApiResponse(code = 401, message = "인증 오류"),
            @ApiResponse(code = 403, message = "재시도 권한 없음"),
            @ApiResponse(code = 404, message = "일정 없음"),
            @ApiResponse(code = 409, message = "재시도 불가")
    })
    @PostMapping("/{schedule_id}/retry")
    public ResponseEntity<AutoTransferRetryResponse> retry(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @RequestHeader(
                    value = "Idempotency-Key",
                    required = false
            ) String idempotencyKey,
            @PathVariable("schedule_id") Long scheduleId
    ) {
        Long memberId =
                memberResolver.resolveMemberId(authorization);

        return ResponseEntity.ok(
                retryService.retry(
                        memberId,
                        scheduleId,
                        idempotencyKey
                )
        );
    }
}