package com.azas.domain.timecapsule.controller;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleRequest;
import com.azas.domain.timecapsule.dto.TimeCapsuleListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleResponse;
import com.azas.domain.timecapsule.service.AccessTokenMemberResolver;
import com.azas.domain.timecapsule.service.TimeCapsuleService;
import com.azas.global.response.ApiErrorResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "타임캡슐")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TimeCapsuleController {

    private final AccessTokenMemberResolver accessTokenMemberResolver;
    private final TimeCapsuleService timeCapsuleService;

    @ApiOperation(
            value = "타임캡슐 보관함 생성",
            notes = "자녀 명의의 활성 적금 계좌에 타임캡슐 보관함을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "보관함 생성 성공",
                    response = TimeCapsuleResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락 또는 오류",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "자녀 접근 권한 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 409,
                    message = "계좌에 이미 보관함이 존재함",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 422,
                    message = "보관함 생성 대상이 아닌 계좌",
                    response = ApiErrorResponse.class
            )
    })
    @PostMapping("/accounts/{account_id}/time-capsule")
    // [JMG] CAPSULE-1 활성 적금 계좌를 기반으로 타임캡슐 보관함 생성 요청을 처리한다.
    public ResponseEntity<TimeCapsuleResponse> createTimeCapsule(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @ApiParam(value = "금융 계좌 ID", required = true)
            @PathVariable("account_id")
            long accountId,
            @Valid
            @RequestBody
            CreateTimeCapsuleRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        timeCapsuleService.createTimeCapsule(
                                memberId,
                                accountId,
                                request
                        )
                );
    }

    @ApiOperation(
            value = "자녀별 타임캡슐 보관함 목록 조회",
            notes = "view=CARD는 최신 생성순, view=CALENDAR는 year와 month 기준 공개 예정일순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "보관함 목록 조회 성공",
                    response = TimeCapsuleListResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "목록 필터 또는 페이지네이션 값 오류",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "자녀 접근 권한 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "자녀를 찾을 수 없음",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/children/{child_id}/time-capsules")
    // [JMG] CAPSULE-2 자녀의 타임캡슐 보관함 목록 조회 요청을 처리한다.
    public ResponseEntity<TimeCapsuleListResponse> getTimeCapsules(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @ApiParam(value = "자녀 ID", required = true)
            @PathVariable("child_id")
            long childId,
            @ApiParam(
                    value = "목록 화면 유형",
                    allowableValues = "CARD,CALENDAR"
            )
            @RequestParam(
                    value = "view",
                    required = false
            )
            String view,
            @ApiParam(
                    value = "보관함 상태",
                    allowableValues = "COLLECTING,RELEASED,ARCHIVED"
            )
            @RequestParam(
                    value = "status",
                    required = false
            )
            String status,
            @RequestParam(
                    value = "cursor",
                    required = false
            )
            String cursor,
            @RequestParam(
                    value = "size",
                    required = false
            )
            Integer size,
            @RequestParam(
                    value = "year",
                    required = false
            )
            Integer year,
            @RequestParam(
                    value = "month",
                    required = false
            )
            Integer month
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                timeCapsuleService.getTimeCapsules(
                        memberId,
                        childId,
                        view,
                        status,
                        cursor,
                        size,
                        year,
                        month
                )
        );
    }

    @ApiOperation(
            value = "타임캡슐 보관함 상세 조회",
            notes = "보관함 메타데이터를 조회합니다. 기록 목록은 별도 엔드포인트로 제공합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "보관함 상세 조회 성공",
                    response = TimeCapsuleResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "자녀 접근 권한 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "보관함을 찾을 수 없음",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/time-capsules/{time_capsule_id}")
    // [JMG] CAPSULE-3 타임캡슐 보관함 상세 조회 요청을 처리한다.
    public ResponseEntity<TimeCapsuleResponse> getTimeCapsule(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 보관함 ID", required = true)
            @PathVariable("time_capsule_id")
            long timeCapsuleId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                timeCapsuleService.getTimeCapsule(
                        memberId,
                        timeCapsuleId
                )
        );
    }
}
