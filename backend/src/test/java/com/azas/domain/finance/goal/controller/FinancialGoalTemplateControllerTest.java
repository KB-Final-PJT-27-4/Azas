package com.azas.domain.finance.goal.controller;

import com.azas.domain.finance.goal.dto.FinancialGoalAmountRecommendationResult;
import com.azas.domain.finance.goal.entity.FinancialGoalAmountRecommendation;
import com.azas.domain.finance.goal.entity.FinancialGoalRecommendationBasis;
import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import com.azas.domain.finance.goal.service.FinancialGoalTemplateService;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinancialGoalTemplateControllerTest {

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    @Mock
    private FinancialGoalTemplateService financialGoalTemplateService;

    @InjectMocks
    private FinancialGoalTemplateController financialGoalTemplateController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(financialGoalTemplateController)
                .build();
    }

    @Test
    void returnsActiveDefaultGoalTemplates() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(8L);
        given(financialGoalTemplateService.getTemplates())
                .willReturn(List.of(
                        template(
                                1L,
                                "대학자금",
                                "대학 등록금과 교육비",
                                "graduation_cap",
                                1
                        ),
                        template(
                                2L,
                                "주거자금",
                                "내 집 마련을 위한 자금",
                                "house",
                                2
                        )
                ));

        mockMvc.perform(get("/api/v1/financial-goal-templates")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templates[0].financial_goal_template_id")
                        .value(1))
                .andExpect(jsonPath("$.templates[0].name")
                        .value("대학자금"))
                .andExpect(jsonPath("$.templates[0].description")
                        .value("대학 등록금과 교육비"))
                .andExpect(jsonPath("$.templates[0].icon_key")
                        .value("graduation_cap"))
                .andExpect(jsonPath("$.templates[0].display_order")
                        .value(1))
                .andExpect(jsonPath("$.templates[1].name")
                        .value("주거자금"))
                .andExpect(jsonPath("$.templates[0].is_default")
                        .doesNotExist())
                .andExpect(jsonPath("$.templates[0].created_by_member_id")
                        .doesNotExist());

        verify(accessTokenMemberResolver).resolveMemberId(
                "Bearer access-token"
        );
    }

    @Test
    void returnsEmptyArrayWhenNoTemplateIsAvailable() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(9L);
        given(financialGoalTemplateService.getTemplates())
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/financial-goal-templates")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templates").isArray())
                .andExpect(jsonPath("$.templates").isEmpty());
    }

    @Test
    void returnsFourStageAmountRecommendationsWithReferenceData()
            throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(8L);

        FinancialGoalTemplate university = template(
                1L,
                "대학자금",
                "대학 등록금과 교육비",
                "graduation_cap",
                1
        );
        FinancialGoalRecommendationBasis basis = basis();
        given(financialGoalTemplateService.getAmountRecommendations(1L))
                .willReturn(new FinancialGoalAmountRecommendationResult(
                        university,
                        basis,
                        List.of(
                                recommendation(
                                        "STARTER",
                                        "시작 준비안",
                                        30_000_000L,
                                        "4년 등록금 중심|교재 및 학습비 일부",
                                        1
                                ),
                                recommendation(
                                        "BALANCED",
                                        "균형 준비안",
                                        50_000_000L,
                                        "등록금|생활비|취업 준비비",
                                        2
                                ),
                                recommendation(
                                        "SECURE",
                                        "든든 준비안",
                                        70_000_000L,
                                        "등록금|생활비|추가 교육비",
                                        3
                                ),
                                recommendation(
                                        "LIFECYCLE",
                                        "생애주기 준비안",
                                        100_000_000L,
                                        "등록금|생활비|주거비|사회초년 자금",
                                        4
                                )
                        )
                ));

        mockMvc.perform(get(
                        "/api/v1/financial-goal-templates/1"
                                + "/amount-recommendations"
                ).header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financial_goal_template_id")
                        .value(1))
                .andExpect(jsonPath("$.goal_name").value("대학자금"))
                .andExpect(jsonPath("$.recommendation_method")
                        .value("STATISTICS_REFERENCE"))
                .andExpect(jsonPath("$.reference_data.organization")
                        .value("교육부·한국대학교육협의회"))
                .andExpect(jsonPath("$.reference_data.metric_value")
                        .value(7106500))
                .andExpect(jsonPath("$.recommendations.length()")
                        .value(4))
                .andExpect(jsonPath(
                        "$.recommendations[0].coverage_items[1]"
                ).value("교재 및 학습비 일부"))
                .andExpect(jsonPath(
                        "$.recommendations[3].recommendation_code"
                ).value("LIFECYCLE"))
                .andExpect(jsonPath(
                        "$.recommendations[3].target_amount"
                ).value(100000000));

        verify(accessTokenMemberResolver).resolveMemberId(
                "Bearer access-token"
        );
        verify(financialGoalTemplateService)
                .getAmountRecommendations(1L);
    }

    private FinancialGoalTemplate template(
            long id,
            String name,
            String description,
            String iconKey,
            int order
    ) {
        FinancialGoalTemplate template = new FinancialGoalTemplate();
        template.setFinancialGoalTemplateId(id);
        template.setGoalName(name);
        template.setDescription(description);
        template.setIconKey(iconKey);
        template.setSortOrder(order);
        return template;
    }

    private FinancialGoalRecommendationBasis basis() {
        FinancialGoalRecommendationBasis basis =
                new FinancialGoalRecommendationBasis();
        basis.setFinancialGoalTemplateId(1L);
        basis.setRecommendationMethod("STATISTICS_REFERENCE");
        basis.setOrganization("교육부·한국대학교육협의회");
        basis.setDatasetName("2025년 4월 대학정보공시 분석 결과");
        basis.setReferenceYear(2025);
        basis.setMetricName("4년제 일반·교육대학 1인당 연평균 등록금");
        basis.setMetricValue(BigDecimal.valueOf(7_106_500L));
        basis.setMetricUnit("원/년");
        basis.setSourceUrl("https://www.moe.go.kr/example");
        basis.setDescription("공공 통계를 참고한 서비스 추천금액입니다.");
        basis.setDisclaimer("실제 비용을 보장하지 않습니다.");
        return basis;
    }

    private FinancialGoalAmountRecommendation recommendation(
            String code,
            String title,
            long targetAmount,
            String coverageItems,
            int displayOrder
    ) {
        FinancialGoalAmountRecommendation recommendation =
                new FinancialGoalAmountRecommendation();
        recommendation.setFinancialGoalTemplateId(1L);
        recommendation.setRecommendationCode(code);
        recommendation.setTitle(title);
        recommendation.setTargetAmount(BigDecimal.valueOf(targetAmount));
        recommendation.setCoverageItems(coverageItems);
        recommendation.setDisplayOrder(displayOrder);
        return recommendation;
    }
}
