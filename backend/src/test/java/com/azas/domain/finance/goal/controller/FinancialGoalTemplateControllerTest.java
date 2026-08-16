package com.azas.domain.finance.goal.controller;

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
}
