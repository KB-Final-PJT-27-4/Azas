package com.azas.domain.finance.goal.mapper;

import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FinancialGoalTemplateMapper {

    List<FinancialGoalTemplate> findActiveDefaultTemplates();
}
