package com.azas.domain.finance.goal.mapper;

import com.azas.domain.finance.goal.entity.FinancialGoalAmountRecommendation;
import com.azas.domain.finance.goal.entity.FinancialGoalRecommendationBasis;
import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinancialGoalTemplateMapper {

    List<FinancialGoalTemplate> findActiveDefaultTemplates();

    FinancialGoalTemplate findActiveDefaultTemplateById(
            @Param("financialGoalTemplateId")
            long financialGoalTemplateId
    );

    FinancialGoalRecommendationBasis findRecommendationBasisByTemplateId(
            @Param("financialGoalTemplateId")
            long financialGoalTemplateId
    );

    List<FinancialGoalAmountRecommendation>
    findActiveAmountRecommendationsByTemplateId(
            @Param("financialGoalTemplateId")
            long financialGoalTemplateId
    );
}
