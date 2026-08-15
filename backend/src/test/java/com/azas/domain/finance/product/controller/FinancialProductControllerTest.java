package com.azas.domain.finance.product.controller;

import com.azas.domain.finance.product.dto.FinancialProductListResponse;
import com.azas.domain.finance.product.entity.FinancialProduct;
import com.azas.domain.finance.product.service.FinancialProductService;
import com.azas.global.security.AccessTokenMemberResolver;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
