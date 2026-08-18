package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import com.azas.domain.finance.goal.mapper.FinancialGoalTemplateMapper;
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
}
