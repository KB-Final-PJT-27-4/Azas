package com.azas.domain.finance.goal.controller;

import com.azas.domain.finance.goal.dto.FinancialGoalCreateRequest;
import com.azas.domain.finance.goal.dto.FinancialGoalCreateResponse;
import com.azas.domain.finance.goal.dto.FinancialGoalDetailResponse;
import com.azas.domain.finance.goal.dto.FinancialGoalListResponse;
import com.azas.domain.finance.goal.dto.FinancialGoalUpdateRequest;
import com.azas.domain.finance.goal.service.FinancialGoalCreateService;
import com.azas.domain.finance.goal.service.FinancialGoalDetailService;
import com.azas.domain.finance.goal.service.FinancialGoalListService;
import com.azas.domain.finance.goal.service.FinancialGoalUpdateService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "목표·자산 형성")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FinancialGoalController {

    private final AccessTokenMemberResolver accessTokenMemberResolver;
    private final FinancialGoalCreateService financialGoalCreateService;
    private final FinancialGoalDetailService financialGoalDetailService;
    private final FinancialGoalListService financialGoalListService;
    private final FinancialGoalUpdateService financialGoalUpdateService;

    @ApiOperation(
            value = "GOAL-5 자녀 금융 목표 수정",
            notes = "부모가 목표 금액, 목표 달성일, 최종 연결 적금 계좌 목록을 부분 수정합니다. "
                    + "목표 종류와 제목은 변경할 수 없으며 연결 적금은 한 개 이상 유지해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "자녀 금융 목표 수정 성공",
                    response = FinancialGoalDetailResponse.class),
            @ApiResponse(code = 400, message = "금융 목표 수정 요청 오류",
                    response = ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "유효하지 않은 Access Token",
                    response = ApiErrorResponse.class),
            @ApiResponse(code = 403, message = "부모 권한 없음",
                    response = ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "목표 또는 적금 계좌를 찾을 수 없음",
                    response = ApiErrorResponse.class),
            @ApiResponse(code = 409, message = "적금 계좌가 다른 목표에 이미 연결됨",
                    response = ApiErrorResponse.class),
            @ApiResponse(code = 422, message = "목표에 연결할 수 없는 계좌 또는 이미 달성한 금액",
                    response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "서버 오류",
                    response = ApiErrorResponse.class)
    })
    @PatchMapping("/financial-goals/{financial_goal_id}")
    public ResponseEntity<FinancialGoalDetailResponse> updateGoal(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("financial_goal_id") long financialGoalId,
            @RequestBody FinancialGoalUpdateRequest request
    ) {
        long requesterMemberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader);
        return ResponseEntity.ok(FinancialGoalDetailResponse.from(
                financialGoalUpdateService.update(
                        requesterMemberId, financialGoalId, request)));
    }

    @ApiOperation(
            value = "GOAL-4 자녀 금융 목표 상세 조회",
            notes = "부모가 접근 가능한 자녀의 단일 금융 목표와 연결 적금 계좌, "
                    + "현재 금액, 남은 금액, 달성률 및 체크포인트를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자녀 금융 목표 상세 조회 성공",
                    response = FinancialGoalDetailResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "금융 목표 ID 오류",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "유효하지 않은 Access Token",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "부모 권한 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "금융 목표를 찾을 수 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/financial-goals/{financial_goal_id}")
    public ResponseEntity<FinancialGoalDetailResponse> getGoal(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("financial_goal_id") long financialGoalId
    ) {
        long requesterMemberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        return ResponseEntity.ok(
                FinancialGoalDetailResponse.from(
                        financialGoalDetailService.getGoal(
                                requesterMemberId,
                                financialGoalId
                        )
                )
        );
    }

    @ApiOperation(
            value = "GOAL-3 자녀 금융 목표 목록 조회",
            notes = "부모가 접근 가능한 자녀의 진행 중·달성 목표와 연결 적금 계좌, "
                    + "현재 금액, 남은 금액 및 달성률을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자녀 금융 목표 목록 조회 성공",
                    response = FinancialGoalListResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "자녀 ID 오류",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "유효하지 않은 Access Token",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "부모 권한 또는 자녀 접근 권한 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "자녀를 찾을 수 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/children/{child_id}/financial-goals")
    public ResponseEntity<FinancialGoalListResponse> getGoals(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") long childId
    ) {
        long requesterMemberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        return ResponseEntity.ok(
                FinancialGoalListResponse.from(
                        financialGoalListService.getGoals(
                                requesterMemberId,
                                childId
                        )
                )
        );
    }

    @ApiOperation(
            value = "GOAL-2 자녀 금융 목표 생성",
            notes = "부모가 자녀의 금융 목표를 생성하고 하나 이상의 활성 적금 계좌를 연결합니다. "
                    + "하나의 적금 계좌는 하나의 활성 목표에만 연결할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "자녀 금융 목표 생성 성공",
                    response = FinancialGoalCreateResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "목표 생성 요청 값 오류",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "유효하지 않은 Access Token",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "부모 권한 또는 자녀 접근 권한 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "자녀·목표 템플릿·적금 계좌를 찾을 수 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 409,
                    message = "선택한 적금 계좌가 이미 다른 목표에 연결됨",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 422,
                    message = "목표 연결이 불가능한 계좌 또는 이미 달성된 목표 금액",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 오류",
                    response = ApiErrorResponse.class
            )
    })
    @PostMapping("/children/{child_id}/financial-goals")
    public ResponseEntity<FinancialGoalCreateResponse> createGoal(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") long childId,
            @RequestBody FinancialGoalCreateRequest request
    ) {
        long requesterMemberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        FinancialGoalCreateResponse response = FinancialGoalCreateResponse.from(
                financialGoalCreateService.create(
                        requesterMemberId,
                        childId,
                        request.toCommand()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
