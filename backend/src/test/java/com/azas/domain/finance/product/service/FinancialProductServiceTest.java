package com.azas.domain.finance.product.service;

import com.azas.domain.finance.product.dto.FinancialProductBookmarkResponse;
import com.azas.domain.finance.product.dto.FinancialProductDetailResponse;
import com.azas.domain.finance.product.dto.FinancialProductRecommendationResponse;
import com.azas.domain.finance.product.entity.FinancialProduct;
import com.azas.domain.finance.product.entity.RecommendationAccountBasis;
import com.azas.domain.finance.product.mapper.FinancialProductMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialProductServiceTest {

    @Mock
    private FinancialProductMapper financialProductMapper;

    private FinancialProductService financialProductService;

    @BeforeEach
    void setUp() {
        financialProductService = new FinancialProductService(
                financialProductMapper,
                new ObjectMapper()
        );
    }

    @Test
    void returnsRecommendationWithNextCursor() throws Exception {
        authorizeChild();
        FinancialProduct firstProduct = product(1L);
        FinancialProduct secondProduct = product(2L);

        when(financialProductMapper.findActiveProducts(
                eq("SAVING"),
                eq(null),
                eq(2)
        )).thenReturn(List.of(firstProduct, secondProduct));
        when(financialProductMapper.findBookmarkedProductIds(1L, 1L))
                .thenReturn(List.of(1L));
        when(financialProductMapper.findChildAge(1L)).thenReturn(10);

        FinancialProductRecommendationResponse response =
                financialProductService.getRecommendations(
                        1L,
                        1L,
                        null,
                        new BigDecimal("100000"),
                        "saving",
                        null,
                        1
                );

        assertEquals(1L, response.getChildId());
        assertEquals("1", response.getNextCursor());
        assertEquals(1, response.getItems().size());
        assertTrue(response.getItems().get(0).isBookmarked());
        String json = new ObjectMapper().writeValueAsString(response);
        assertTrue(json.contains("\"is_bookmarked\":true"));
        assertFalse(json.contains("\"bookmarked\":"));
    }

    @Test
    void rejectsUnknownProductType() {
        authorizeChild();
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialProductService.getRecommendations(
                        1L, 1L, null, null, "LOAN", null, null
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );
        verify(financialProductMapper, never()).findActiveProducts(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                anyInt()
        );
    }

    @Test
    void makesBookmarkIdempotently() {
        authorizeChild();
        when(financialProductMapper.findActiveProductById(1L))
                .thenReturn(product(1L));

        FinancialProductBookmarkResponse response =
                financialProductService.updateBookmark(1L, 1L, 1L, true);

        assertTrue(response.isBookmarked());
        verify(financialProductMapper).insertBookmarkIfAbsent(1L, 1L, 1L);
    }

    @Test
    void rejectsAccountContextWithoutChildContext() {
        when(financialProductMapper.findActiveProductById(1L))
                .thenReturn(product(1L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialProductService.getProductDetail(
                        1L, 1L, null, 3L
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );
        verify(financialProductMapper, never()).findSavingsAccountBasis(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    @Test
    void returnsDetailWithoutRecommendationWhenNoAccountIsProvided() {
        when(financialProductMapper.findActiveProductById(1L))
                .thenReturn(product(1L));

        FinancialProductDetailResponse response =
                financialProductService.getProductDetail(1L, 1L, null, null);

        assertNull(response.getRecommendation());
    }

    private FinancialProduct product(long productId) {
        FinancialProduct product = new FinancialProduct();
        product.setFinancialProductId(productId);
        product.setProductType("SAVING");
        product.setName("Product " + productId);
        product.setBaseInterestRate(new BigDecimal("2.1"));
        product.setMaxInterestRate(new BigDecimal("3.4"));
        product.setMinAge(0);
        product.setMaxAge(19);
        product.setMinMonthlyAmount(new BigDecimal("10000"));
        product.setMaxMonthlyAmount(new BigDecimal("3000000"));
        product.setEligibilityConditionsJson("[]");
        product.setDepositConditionsJson("[]");
        product.setPreferentialConditionsJson("[]");
        product.setAdditionalBenefitsJson("[]");
        product.setCautionsJson("[]");
        return product;
    }

    private void authorizeChild() {
        when(financialProductMapper.countActiveChild(1L)).thenReturn(1);
        when(financialProductMapper.countChildAccess(1L, 1L)).thenReturn(1);
    }
}
