package com.azas.domain.finance.goal.dto;

import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialGoalTemplateListResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesOnlyFieldsRequiredByGoalSelectionScreen()
            throws Exception {
        FinancialGoalTemplate template = template();

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(
                FinancialGoalTemplateListResponse.from(List.of(template))
        ));

        JsonNode item = json.path("templates").get(0);
        assertEquals(1L, item.path("financial_goal_template_id").asLong());
        assertEquals("대학자금", item.path("name").asText());
        assertEquals("대학 등록금과 교육비", item.path("description").asText());
        assertEquals("graduation_cap", item.path("icon_key").asText());
        assertEquals(1, item.path("display_order").asInt());
        assertFalse(item.has("goal_name"));
        assertFalse(item.has("sort_order"));
        assertFalse(item.has("is_default"));
        assertFalse(item.has("is_active"));
        assertFalse(item.has("created_by_member_id"));
    }

    @Test
    void serializesEmptyTemplateList() throws Exception {
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(
                FinancialGoalTemplateListResponse.from(List.of())
        ));

        assertTrue(json.path("templates").isArray());
        assertEquals(0, json.path("templates").size());
    }

    private FinancialGoalTemplate template() {
        FinancialGoalTemplate template = new FinancialGoalTemplate();
        template.setFinancialGoalTemplateId(1L);
        template.setGoalName("대학자금");
        template.setDescription("대학 등록금과 교육비");
        template.setIconKey("graduation_cap");
        template.setSortOrder(1);
        return template;
    }
}
