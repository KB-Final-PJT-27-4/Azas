package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.goal.dto.FinancialGoalAmountRecommendationResult;
import com.azas.domain.finance.goal.entity.FinancialGoalAmountRecommendation;
import com.azas.domain.finance.goal.entity.FinancialGoalRecommendationBasis;
import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import com.azas.domain.finance.goal.mapper.FinancialGoalTemplateMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FinancialGoalTemplateServiceTest {

    @Mock
    private FinancialGoalTemplateMapper financialGoalTemplateMapper;

    @InjectMocks
    private FinancialGoalTemplateService financialGoalTemplateService;

    @Test
    void returnsActiveDefaultTemplatesInMapperOrder() {
        FinancialGoalTemplate university = template(1L, "대학자금", 1);
        FinancialGoalTemplate housing = template(2L, "주거자금", 2);
        given(financialGoalTemplateMapper.findActiveDefaultTemplates())
                .willReturn(List.of(university, housing));

        List<FinancialGoalTemplate> result =
                financialGoalTemplateService.getTemplates();

        assertEquals(List.of(university, housing), result);
        verify(financialGoalTemplateMapper).findActiveDefaultTemplates();
    }

    @Test
    void returnsEmptyListWhenNoTemplateIsAvailable() {
        given(financialGoalTemplateMapper.findActiveDefaultTemplates())
                .willReturn(List.of());

        assertTrue(financialGoalTemplateService.getTemplates().isEmpty());
    }

    @Test
    void returnsAmountRecommendationsWithTemplateAndBasis() {
        FinancialGoalTemplate university = template(1L, "대학자금", 1);
        FinancialGoalRecommendationBasis basis = basis(1L);
        List<FinancialGoalAmountRecommendation> recommendations = List.of(
                recommendation("STARTER", 30_000_000L, 1),
                recommendation("BALANCED", 50_000_000L, 2),
                recommendation("SECURE", 70_000_000L, 3),
                recommendation("LIFECYCLE", 100_000_000L, 4)
        );
        given(financialGoalTemplateMapper
                .findActiveDefaultTemplateById(1L))
                .willReturn(university);
        given(financialGoalTemplateMapper
                .findRecommendationBasisByTemplateId(1L))
                .willReturn(basis);
        given(financialGoalTemplateMapper
                .findActiveAmountRecommendationsByTemplateId(1L))
                .willReturn(recommendations);

        FinancialGoalAmountRecommendationResult result =
                financialGoalTemplateService
                        .getAmountRecommendations(1L);

        assertEquals(university, result.getTemplate());
        assertEquals(basis, result.getBasis());
        assertEquals(recommendations, result.getRecommendations());
    }

    @Test
    void rejectsNonPositiveTemplateId() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialGoalTemplateService
                        .getAmountRecommendations(0L)
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
    }

    @Test
    void rejectsUnknownTemplate() {
        given(financialGoalTemplateMapper
                .findActiveDefaultTemplateById(99L))
                .willReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialGoalTemplateService
                        .getAmountRecommendations(99L)
        );

        assertEquals(
                ErrorCode.FINANCIAL_GOAL_TEMPLATE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsTemplateWithoutRecommendationBasis() {
        given(financialGoalTemplateMapper
                .findActiveDefaultTemplateById(1L))
                .willReturn(template(1L, "대학자금", 1));
        given(financialGoalTemplateMapper
                .findRecommendationBasisByTemplateId(1L))
                .willReturn(null);
        given(financialGoalTemplateMapper
                .findActiveAmountRecommendationsByTemplateId(1L))
                .willReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialGoalTemplateService
                        .getAmountRecommendations(1L)
        );

        assertEquals(
                ErrorCode.FINANCIAL_GOAL_RECOMMENDATION_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    private FinancialGoalTemplate template(long id, String name, int order) {
        FinancialGoalTemplate template = new FinancialGoalTemplate();
        template.setFinancialGoalTemplateId(id);
        template.setGoalName(name);
        template.setSortOrder(order);
        return template;
    }

    private FinancialGoalRecommendationBasis basis(long templateId) {
        FinancialGoalRecommendationBasis basis =
                new FinancialGoalRecommendationBasis();
        basis.setFinancialGoalTemplateId(templateId);
        basis.setRecommendationMethod("STATISTICS_REFERENCE");
        return basis;
    }

    private FinancialGoalAmountRecommendation recommendation(
            String code,
            long targetAmount,
            int displayOrder
    ) {
        FinancialGoalAmountRecommendation recommendation =
                new FinancialGoalAmountRecommendation();
        recommendation.setRecommendationCode(code);
        recommendation.setTargetAmount(BigDecimal.valueOf(targetAmount));
        recommendation.setDisplayOrder(displayOrder);
        return recommendation;
    }
}
