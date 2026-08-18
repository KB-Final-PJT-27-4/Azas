package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import com.azas.domain.finance.goal.mapper.FinancialGoalTemplateMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private FinancialGoalTemplate template(long id, String name, int order) {
        FinancialGoalTemplate template = new FinancialGoalTemplate();
        template.setFinancialGoalTemplateId(id);
        template.setGoalName(name);
        template.setSortOrder(order);
        return template;
    }
}
