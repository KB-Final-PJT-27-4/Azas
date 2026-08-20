package com.azas.domain.checklist.mapper;

import com.azas.domain.checklist.dto.ChecklistItemRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(
        named = "RUN_MYSQL_INTEGRATION_TESTS",
        matches = "true"
)
class ChecklistMapperIntegrationTest {

    private ChecklistMapper checklistMapper;

    @Test
    void 실제_MySQL에서_체크리스트와_상세_항목을_조회한다() {
        assertDoesNotThrow(() -> {
            List<ChecklistItemRow> rows =
                    checklistMapper.findItems(
                            6L,
                            "PREGNANCY"
                    );

            assertNotNull(rows);
        });
    }
}