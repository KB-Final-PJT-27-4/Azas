package com.azas.global.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseSchemaConsistencyTest {

    @Test
    void schemaReflectsCurrentMockFinanceAndAllowanceModel() throws Exception {
        String schema = normalize(readResource("db/schema.sql"));

        assertFalse(schema.contains("CREATE TABLE financial_connection"));
        assertFalse(schema.contains("CREATE TABLE financial_sync_job"));
        assertFalse(schema.contains("last_allowance_request_month"));
        assertFalse(schema.contains("last_allowance_requested_at"));
        assertFalse(schema.contains("synced_at"));
        assertTrue(schema.contains(
                "source_type VARCHAR(30) NOT NULL DEFAULT 'MOCK'"
        ));
        assertTrue(schema.contains("recorded_at DATETIME(6) NOT NULL"));
        assertTrue(schema.contains("ck_account_transaction_source_type"));
        assertFalse(schema.contains("goal_name_snapshot"));
        assertFalse(schema.matches(".*\\bgoal_target_amount\\b.*"));
        assertFalse(schema.matches(".*\\bgoal_target_date\\b.*"));
        assertFalse(schema.contains(
                "idx_financial_account_goal_template_id"
        ));
        assertTrue(schema.contains("CREATE TABLE financial_goal_account"));
        assertTrue(schema.contains(
                "UNIQUE KEY uk_financial_goal_account_account"
        ));
    }

    @Test
    void schemaAndSeedReflectSingleImageTimeCapsuleModel() throws Exception {
        String schema = normalize(readResource("db/schema.sql"));
        String seed = normalize(readResource("db/seed.sql"));

        assertTrue(schema.contains(
                "ck_capsule_entry_media_mode CHECK (media_mode IN ('NONE', 'IMAGE'))"
        ));
        assertTrue(schema.contains(
                "ck_capsule_media_type CHECK (media_type = 'IMAGE')"
        ));
        assertTrue(schema.contains(
                "ck_capsule_media_slot CHECK (slot_no = 1)"
        ));
        assertFalse(schema.contains("'VIDEO'"));
        assertFalse(seed.contains("'IMPORTED'"));
        assertFalse(seed.contains("synced_at"));
    }

    @Test
    void withdrawalMapperDoesNotReferenceRemovedFinancialConnection()
            throws Exception {
        String mapper = readResource(
                "mapper/member/MemberWithdrawalMapper.xml"
        );

        assertFalse(mapper.contains("financial_connection"));
    }

    @Test
    void goalAndTransferMappersUseCanonicalGoalAccountRelation()
            throws Exception {
        String goalMapper = readResource(
                "mapper/finance/goal/FinancialGoalMapper.xml"
        );
        String transferMapper = readResource(
                "mapper/finance/transfer/TransferMapper.xml"
        );

        assertFalse(goalMapper.contains("updateAccountGoalSnapshot"));
        assertFalse(goalMapper.contains("clearAccountGoalSnapshot"));
        assertFalse(transferMapper.contains("goal_name_snapshot"));
        assertFalse(transferMapper.contains(
                "dst.financial_goal_template_id"
        ));
        assertTrue(transferMapper.contains("financial_goal_account"));
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", " ");
    }

    private String readResource(String path) throws IOException {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
