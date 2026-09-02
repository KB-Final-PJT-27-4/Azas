package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.goal.dto.FinancialGoalAmountRecommendationResult;
import com.azas.domain.finance.goal.entity.FinancialGoalAmountRecommendation;
import com.azas.domain.finance.goal.entity.FinancialGoalRecommendationBasis;
import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import com.azas.domain.finance.goal.mapper.FinancialGoalTemplateMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialGoalTemplateService {

    private final FinancialGoalTemplateMapper financialGoalTemplateMapper;

    @Transactional(readOnly = true)
    public List<FinancialGoalTemplate> getTemplates() {
        return financialGoalTemplateMapper.findActiveDefaultTemplates();
    }

    @Transactional(readOnly = true)
    public FinancialGoalAmountRecommendationResult getAmountRecommendations(
            long financialGoalTemplateId
    ) {
        if (financialGoalTemplateId <= 0) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        FinancialGoalTemplate template = financialGoalTemplateMapper
                .findActiveDefaultTemplateById(financialGoalTemplateId);
        if (template == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_GOAL_TEMPLATE_NOT_FOUND
            );
        }

        FinancialGoalRecommendationBasis basis = financialGoalTemplateMapper
                .findRecommendationBasisByTemplateId(
                        financialGoalTemplateId
                );
        List<FinancialGoalAmountRecommendation> recommendations =
                financialGoalTemplateMapper
                        .findActiveAmountRecommendationsByTemplateId(
                                financialGoalTemplateId
                        );
        if (basis == null || recommendations == null
                || recommendations.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_GOAL_RECOMMENDATION_NOT_FOUND
            );
        }

        return new FinancialGoalAmountRecommendationResult(
                template,
                basis,
                recommendations
        );
    }
}
