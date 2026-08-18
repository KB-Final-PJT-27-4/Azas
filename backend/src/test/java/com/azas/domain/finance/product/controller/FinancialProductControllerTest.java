package com.azas.domain.finance.product.controller;

import com.azas.domain.finance.product.dto.FinancialProductDetailResponse;
import com.azas.domain.finance.product.dto.FinancialProductListResponse;
import com.azas.domain.finance.product.dto.FinancialProductMaturityEstimateRequest;
import com.azas.domain.finance.product.dto.FinancialProductMaturityEstimateResponse;
import com.azas.domain.finance.product.entity.FinancialProduct;
import com.azas.domain.finance.product.service.FinancialProductService;
import com.azas.global.security.AccessTokenMemberResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinancialProductControllerTest {

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    @Mock
    private FinancialProductService financialProductService;

    @InjectMocks
    private FinancialProductController financialProductController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(financialProductController)
                .build();
    }

    @Test
    void servesGeneralFinancialProductListAndChildBookmarks() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(1L);
        FinancialProduct product = product();
        given(financialProductService.getProducts(
                "SAVING", null, null
        )).willReturn(new FinancialProductListResponse(
                List.of(FinancialProductListResponse.Item.from(
                        product,
                        List.of("#만19세미만", "#자유적립")
                )),
                null,
                false
        ));

        mockMvc.perform(get("/api/v1/financial-products")
                        .param("product_type", "SAVING")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].financial_product_id")
                        .value(1))
                .andExpect(jsonPath("$.items[0].highlight_label")
                        .value("자녀 추천"))
                .andExpect(jsonPath("$.items[0].hashtags[0]")
                        .value("#만19세미만"))
                .andExpect(jsonPath("$.items[0].contract_period.min_months")
                        .value(12))
                .andExpect(jsonPath("$.has_next").value(false))
                .andExpect(jsonPath("$.items[0].match_score").doesNotExist())
                .andExpect(jsonPath("$.items[0].is_bookmarked")
                        .doesNotExist());

        mockMvc.perform(get(
                        "/api/v1/children/{childId}/financial-products/bookmarks",
                        1L
                )
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk());
    }

    @Test
    void noLongerServesLegacySingularPaths() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/children/{childId}/financial-product-recommendations",
                        1L
                ))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(
                        "/api/v1/children/{childId}/financial-products/recommendations",
                        1L
                ))
                .andExpect(status().isNotFound());
    }

    @Test
    void servesProductDetailWithOptionalChildBookmarkContext()
            throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(1L);
        FinancialProduct product = product();
        product.setCurationReason("검수된 정적 큐레이션 사유");
        product.setInterestRateReference("12개월 기준 · 세금공제 전");
        product.setMinMonthlyAmount(new BigDecimal("10000"));
        product.setMaxMonthlyAmount(new BigDecimal("3000000"));
        product.setEligibilityConditionsJson("[]");
        product.setInterestPaymentMethod("MATURITY_LUMP_SUM");
        product.setJoinTerminationMethod("KB스타뱅킹에서 가입·해지");
        product.setPreferentialConditionsJson("[]");
        product.setAdditionalBenefitsJson("[]");
        product.setCautionsJson("[]");
        FinancialProductDetailResponse response =
                FinancialProductDetailResponse.from(
                        product,
                        true,
                        new ObjectMapper()
                );
        given(financialProductService.getProductDetail(1L, 1L, 6L))
                .willReturn(response);

        mockMvc.perform(get(
                        "/api/v1/financial-products/{productId}",
                        1L
                )
                        .param("child_id", "6")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financial_product_id").value(1))
                .andExpect(jsonPath("$.curation_reason")
                        .value("검수된 정적 큐레이션 사유"))
                .andExpect(jsonPath("$.interest_rate.reference")
                        .value("12개월 기준 · 세금공제 전"))
                .andExpect(jsonPath("$.monthly_deposit.min_amount")
                        .value(10000))
                .andExpect(jsonPath("$.is_bookmarked").value(true))
                .andExpect(jsonPath("$.recommendation").doesNotExist())
                .andExpect(jsonPath("$.external_product_id")
                        .doesNotExist());
    }

    @Test
    void servesFinancialProductMaturityEstimate() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(1L);
        FinancialProductMaturityEstimateResponse response =
                new FinancialProductMaturityEstimateResponse(
                        1L,
                        new BigDecimal("300000"),
                        12,
                        "MAX",
                        "최고금리",
                        new BigDecimal("3.4"),
                        new BigDecimal("3600000"),
                        new BigDecimal("66300"),
                        new BigDecimal("10210"),
                        new BigDecimal("56090"),
                        new BigDecimal("3656090")
                );
        given(financialProductService.estimateMaturity(
                org.mockito.ArgumentMatchers.eq(1L),
                any(FinancialProductMaturityEstimateRequest.class)
        )).willReturn(response);

        mockMvc.perform(post(
                        "/api/v1/financial-products/{productId}/maturity-estimate",
                        1L
                )
                        .header("Authorization", "Bearer access-token")
                        .contentType("application/json")
                        .content("{\"monthly_amount\":300000,"
                                + "\"period_months\":12}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/json"
                ))
                .andExpect(jsonPath("$.financial_product_id").value(1))
                .andExpect(jsonPath("$.monthly_amount").value(300000))
                .andExpect(jsonPath("$.period_months").value(12))
                .andExpect(jsonPath("$.applied_interest_rate.type")
                        .value("MAX"))
                .andExpect(jsonPath("$.applied_interest_rate.label")
                        .value("최고금리"))
                .andExpect(jsonPath("$.applied_interest_rate.annual_rate")
                        .value(3.4))
                .andExpect(jsonPath("$.principal_amount").value(3600000))
                .andExpect(jsonPath("$.estimated_interest_before_tax")
                        .value(66300))
                .andExpect(jsonPath("$.tax_rate").value(15.4))
                .andExpect(jsonPath("$.estimated_tax").value(10210))
                .andExpect(jsonPath("$.estimated_interest_after_tax")
                        .value(56090))
                .andExpect(jsonPath("$.estimated_maturity_amount")
                        .value(3656090))
                .andExpect(jsonPath("$.calculation_basis")
                        .value("MONTHLY_BEGINNING_SIMPLE_INTEREST"))
                .andExpect(jsonPath("$.disclaimer").isNotEmpty())
                .andExpect(jsonPath("$.interest_rate_type")
                        .doesNotExist());
    }

    @Test
    void rejectsInvalidMaturityEstimateRequestAtWebBoundary()
            throws Exception {
        mockMvc.perform(post(
                        "/api/v1/financial-products/{productId}/maturity-estimate",
                        1L
                )
                        .header("Authorization", "Bearer access-token")
                        .contentType("application/json")
                        .content("{\"monthly_amount\":0,"
                                + "\"period_months\":0}"))
                .andExpect(status().isBadRequest());
    }

    private FinancialProduct product() {
        FinancialProduct product = new FinancialProduct();
        product.setFinancialProductId(1L);
        product.setBankName("KB국민은행");
        product.setName("KB Young Youth 적금");
        product.setProductType("SAVING");
        product.setProductSubtype("자유적립식 예금");
        product.setTargetOwnerType("CHILD");
        product.setHighlightLabel("자녀 추천");
        product.setSummary("어린이·청소년을 위한 장기 저축 적금 상품입니다.");
        product.setMaxInterestRate(new BigDecimal("3.6500"));
        product.setMinContractPeriodMonths(12);
        product.setMaxContractPeriodMonths(12);
        return product;
    }
}
