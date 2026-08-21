package com.azas.domain.finance.goal.controller;

import com.azas.domain.finance.goal.dto.FinancialGoalAmountRecommendationResponse;
import com.azas.domain.finance.goal.dto.FinancialGoalTemplateListResponse;
import com.azas.domain.finance.goal.service.FinancialGoalTemplateService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "목표·자산 형성")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FinancialGoalTemplateController {

    private final AccessTokenMemberResolver accessTokenMemberResolver;
    private final FinancialGoalTemplateService financialGoalTemplateService;

    @ApiOperation(
            value = "GOAL-1 금융 목표 템플릿 조회",
            notes = "적금 목표 생성 화면에서 사용할 서비스 기본 목표 템플릿을 "
                    + "표시 순서대로 조회합니다. 직접 입력 목표는 포함하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "금융 목표 템플릿 조회 성공",
                    response = FinancialGoalTemplateListResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·위조 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/financial-goal-templates")
    public ResponseEntity<FinancialGoalTemplateListResponse> getTemplates(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader
    ) {
        accessTokenMemberResolver.resolveMemberId(authorizationHeader);
        return ResponseEntity.ok(FinancialGoalTemplateListResponse.from(
                financialGoalTemplateService.getTemplates()
        ));
    }

    @ApiOperation(
            value = "GOAL-2 목표 금액 추천 조회",
            notes = "선택한 기본 금융 목표에 대해 공공 통계를 참고하여 "
                    + "서비스가 구성한 4단계 추천 금액과 산정 근거를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "목표 금액 추천 조회 성공",
                    response = FinancialGoalAmountRecommendationResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "올바르지 않은 목표 템플릿 ID",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·위조 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "목표 템플릿 또는 추천 금액 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping(
            "/financial-goal-templates/{financial_goal_template_id}"
                    + "/amount-recommendations"
    )
    public ResponseEntity<FinancialGoalAmountRecommendationResponse>
    getAmountRecommendations(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("financial_goal_template_id")
            long financialGoalTemplateId
    ) {
        accessTokenMemberResolver.resolveMemberId(authorizationHeader);
        return ResponseEntity.ok(
                FinancialGoalAmountRecommendationResponse.from(
                        financialGoalTemplateService
                                .getAmountRecommendations(
                                        financialGoalTemplateId
                                )
                )
        );
    }
}
