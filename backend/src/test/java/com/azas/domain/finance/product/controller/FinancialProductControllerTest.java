package com.azas.domain.finance.product.controller;

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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void servesUnifiedChildFinancialProductPaths() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(1L);

        mockMvc.perform(get(
                        "/api/v1/children/{childId}/financial-products/recommendations",
                        1L
                )
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk());

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
    }
}
