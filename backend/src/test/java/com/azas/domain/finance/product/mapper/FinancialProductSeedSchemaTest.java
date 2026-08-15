package com.azas.domain.finance.product.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialProductSeedSchemaTest {

    @Test
    void schemaDefinesProductListDisplayAndContractPeriodColumns() throws Exception {
        String schema = readResource("db/schema.sql");

        assertTrue(schema.contains("highlight_label VARCHAR(50) NULL"));
        assertTrue(schema.contains("display_badges_json JSON NOT NULL"));
        assertTrue(schema.contains("min_contract_period_months INT NULL"));
        assertTrue(schema.contains("max_contract_period_months INT NULL"));
        assertTrue(schema.contains("ck_financial_product_contract_period"));
    }

    @Test
    void seedContainsCuratedKbSavingsProducts() throws Exception {
        String seed = readResource("db/seed.sql");

        assertTrue(seed.contains("'KB Young Youth 적금'"));
        assertTrue(seed.matches(
                "(?s).*'KB Young Youth 적금'.*?2\\.1000,\\s+3\\.6500,.*"
        ));
        assertTrue(seed.contains("'KB아이사랑적금'"));
        assertTrue(seed.contains("'내 아이를 위한 280일 적금'"));
        assertTrue(seed.contains("JSON_ARRAY('#만19세미만', '#자유적립', '#무료보험')"));
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
