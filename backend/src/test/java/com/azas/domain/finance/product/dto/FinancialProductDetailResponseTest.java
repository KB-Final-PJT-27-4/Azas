package com.azas.domain.finance.product.dto;

import com.azas.domain.finance.product.entity.FinancialProduct;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialProductDetailResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void serializesOnlyConfirmedProductDetailFields() throws Exception {
        FinancialProductDetailResponse response =
                FinancialProductDetailResponse.from(
                        product(),
                        true,
                        objectMapper
                );

        JsonNode json = objectMapper.readTree(
                objectMapper.writeValueAsString(response)
        );

        assertEquals(1L, json.get("financial_product_id").asLong());
        assertEquals("CHILD", json.get("target_owner_type").asText());
        assertEquals("깨비 추천", json.get("badges").get(0)
                .get("label").asText());
        assertEquals(3.65, json.get("interest_rate")
                .get("max_rate").asDouble());
        assertEquals(12, json.get("contract_period")
                .get("min_months").asInt());
        assertEquals(10000, json.get("monthly_deposit")
                .get("min_amount").asInt());
        assertTrue(json.get("is_bookmarked").asBoolean());
        assertFalse(json.has("recommendation"));
        assertFalse(json.has("external_product_id"));
        assertFalse(json.has("product_image_key"));
        assertFalse(json.has("updated_at"));
    }

    private FinancialProduct product() {
        FinancialProduct product = new FinancialProduct();
        product.setFinancialProductId(1L);
        product.setBankName("KB국민은행");
        product.setName("KB Young Youth 적금");
        product.setProductType("SAVING");
        product.setProductSubtype("자유적립식 예금");
        product.setTargetOwnerType("CHILD");
        product.setSummary("어린이·청소년을 위한 적금입니다.");
        product.setHighlightLabel("깨비 추천");
        product.setCurationReason("검수된 정적 큐레이션 사유입니다.");
        product.setBaseInterestRate(new BigDecimal("2.10"));
        product.setMaxInterestRate(new BigDecimal("3.65"));
        product.setInterestRateReference("12개월 기준 · 세금공제 전");
        product.setMinAge(0);
        product.setMaxAge(19);
        product.setMinMonthlyAmount(new BigDecimal("10000"));
        product.setMaxMonthlyAmount(new BigDecimal("3000000"));
        product.setMinContractPeriodMonths(12);
        product.setMaxContractPeriodMonths(12);
        product.setRenewalDescription("1년 단위 자동 재예치");
        product.setEligibilityConditionsJson("[]");
        product.setInterestPaymentMethod("MATURITY_LUMP_SUM");
        product.setJoinTerminationMethod("KB스타뱅킹에서 가입·해지");
        product.setPreferentialConditionsJson("[]");
        product.setAdditionalBenefitsJson("[]");
        product.setCautionsJson("[]");
        product.setDetailUrl("https://www.kbstar.com/");
        product.setSourceBaseDate(LocalDate.of(2026, 8, 15));
        return product;
    }
}
