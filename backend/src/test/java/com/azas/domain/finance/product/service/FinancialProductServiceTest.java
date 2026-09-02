package com.azas.domain.finance.product.service;

import com.azas.domain.finance.product.dto.FinancialProductBookmarkResponse;
import com.azas.domain.finance.product.dto.FinancialProductDetailResponse;
import com.azas.domain.finance.product.dto.FinancialProductListResponse;
import com.azas.domain.finance.product.dto.FinancialProductMaturityEstimateRequest;
import com.azas.domain.finance.product.dto.FinancialProductMaturityEstimateResponse;
import com.azas.domain.finance.product.entity.FinancialProduct;
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
                new ObjectMapper(),
                new MaturityEstimateCalculator()
        );
    }

    @Test
    void returnsProductListWithNextCursor() throws Exception {
        FinancialProduct firstProduct = product(1L);
        FinancialProduct secondProduct = product(2L);

        when(financialProductMapper.findActiveProducts(
                eq("SAVING"),
                eq(null),
                eq(2)
        )).thenReturn(List.of(firstProduct, secondProduct));
        FinancialProductListResponse response =
                financialProductService.getProducts("saving", null, 1);

        assertEquals("1", response.getNextCursor());
        assertTrue(response.isHasNext());
        assertEquals(1, response.getItems().size());
        assertEquals(
                List.of("#만19세미만", "#자유적립"),
                response.getItems().get(0).getHashtags()
        );
        assertEquals(
                12,
                response.getItems().get(0)
                        .getContractPeriod().getMinMonths()
        );
        String json = new ObjectMapper().writeValueAsString(response);
        assertTrue(json.contains("\"has_next\":true"));
        assertFalse(json.contains("match_score"));
        assertFalse(json.contains("recommendation_reason"));
        assertFalse(json.contains("is_bookmarked"));
    }

    @Test
    void mapsDemandDepositAliasToAccountProductType() {
        when(financialProductMapper.findActiveProducts(
                "ACCOUNT", null, 21
        )).thenReturn(Collections.emptyList());

        FinancialProductListResponse response =
                financialProductService.getProducts(
                        "DEMAND_DEPOSIT", null, 20
                );

        assertTrue(response.getItems().isEmpty());
        verify(financialProductMapper).findActiveProducts(
                "ACCOUNT", null, 21
        );
    }

    @Test
    void mapsSavingsAliasToSavingProductType() {
        when(financialProductMapper.findActiveProducts(
                "SAVING", null, 21
        )).thenReturn(Collections.emptyList());

        FinancialProductListResponse response =
                financialProductService.getProducts("SAVINGS", null, 20);

        assertTrue(response.getItems().isEmpty());
        verify(financialProductMapper).findActiveProducts(
                "SAVING", null, 21
        );
    }

    @Test
    void rejectsUnknownProductType() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialProductService.getProducts(
                        "LOAN", null, null
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
    void returnsParentAndChildProductsWithoutOwnerFiltering() {
        FinancialProduct parentProduct = product(3L);
        parentProduct.setTargetOwnerType("PARENT");
        when(financialProductMapper.findActiveProducts(null, null, 21))
                .thenReturn(List.of(parentProduct));

        FinancialProductListResponse response =
                financialProductService.getProducts(null, null, null);

        assertEquals(1, response.getItems().size());
        assertEquals(
                "PARENT",
                response.getItems().get(0).getTargetOwnerType()
        );
        assertFalse(response.isHasNext());
        assertNull(response.getNextCursor());
    }

    @Test
    void rejectsMalformedDisplayBadgesJson() {
        FinancialProduct malformedProduct = product(1L);
        malformedProduct.setDisplayBadgesJson("not-json");
        when(financialProductMapper.findActiveProducts(null, null, 21))
                .thenReturn(List.of(malformedProduct));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialProductService.getProducts(null, null, null)
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
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
    void rejectsParentOnlyProductBookmarkInChildContext() {
        authorizeChild();
        FinancialProduct product = product(3L);
        product.setTargetOwnerType("PARENT");
        when(financialProductMapper.findActiveProductById(3L))
                .thenReturn(product);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialProductService.updateBookmark(
                        1L, 1L, 3L, true
                )
        );

        assertEquals(
                ErrorCode.FINANCIAL_PRODUCT_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(financialProductMapper, never()).insertBookmarkIfAbsent(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    @Test
    void returnsParentProductDetailButDoesNotQueryChildBookmark() {
        authorizeChild();
        FinancialProduct product = product(3L);
        product.setTargetOwnerType("PARENT");
        when(financialProductMapper.findActiveProductById(3L))
                .thenReturn(product);

        FinancialProductDetailResponse response =
                financialProductService.getProductDetail(1L, 3L, 1L);

        assertEquals("PARENT", response.getTargetOwnerType());
        assertFalse(response.isBookmarked());
        verify(financialProductMapper, never()).countBookmark(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    @Test
    void rejectsInvalidDetailIdentifiers() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialProductService.getProductDetail(
                        1L, 0L, null
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );
        verify(financialProductMapper, never()).findActiveProductById(
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    @Test
    void returnsCuratedDetailWithoutPersonalizedRecommendation() {
        when(financialProductMapper.findActiveProductById(1L))
                .thenReturn(product(1L));

        FinancialProductDetailResponse response =
                financialProductService.getProductDetail(1L, 1L, null);

        assertEquals("정적 큐레이션 사유", response.getCurationReason());
        assertEquals(12, response.getContractPeriod().getMinMonths());
        assertEquals(
                "12개월 기준 · 세금공제 전",
                response.getInterestRate().getReference()
        );
        assertFalse(response.isBookmarked());
    }

    @Test
    void returnsBookmarkStateWhenChildContextIsProvided() {
        authorizeChild();
        when(financialProductMapper.findActiveProductById(1L))
                .thenReturn(product(1L));
        when(financialProductMapper.countBookmark(1L, 1L, 1L))
                .thenReturn(1);

        FinancialProductDetailResponse response =
                financialProductService.getProductDetail(1L, 1L, 1L);

        assertTrue(response.isBookmarked());
    }

    @Test
    void rejectsMalformedDetailConditionJson() {
        FinancialProduct product = product(1L);
        product.setEligibilityConditionsJson("not-json");
        when(financialProductMapper.findActiveProductById(1L))
                .thenReturn(product);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialProductService.getProductDetail(1L, 1L, null)
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    @Test
    void estimatesMaturityUsingMaximumRate() {
        FinancialProduct product = product(1L);
        product.setContractPeriodMonths(12);
        when(financialProductMapper.findActiveProductById(1L))
                .thenReturn(product);

        FinancialProductMaturityEstimateResponse response =
                financialProductService.estimateMaturity(
                        1L,
                        new FinancialProductMaturityEstimateRequest(
                                new BigDecimal("300000"),
                                12
                        )
                );

        assertEquals("MAX", response.getAppliedInterestRate().getType());
        assertEquals(
                new BigDecimal("3.4"),
                response.getAppliedInterestRate().getAnnualRate()
        );
        assertEquals(
                new BigDecimal("3600000"),
                response.getPrincipalAmount()
        );
        assertEquals(
                new BigDecimal("66300"),
                response.getEstimatedInterestBeforeTax()
        );
        assertEquals(
                new BigDecimal("3656090"),
                response.getEstimatedMaturityAmount()
        );
    }

    @Test
    void fallsBackToBaseRateWhenMaximumRateIsMissing() {
        FinancialProduct product = product(1L);
        product.setMaxInterestRate(null);
        product.setContractPeriodMonths(12);
        when(financialProductMapper.findActiveProductById(1L))
                .thenReturn(product);

        FinancialProductMaturityEstimateResponse response =
                financialProductService.estimateMaturity(
                        1L,
                        new FinancialProductMaturityEstimateRequest(
                                new BigDecimal("100000"),
                                12
                        )
                );

        assertEquals("BASE", response.getAppliedInterestRate().getType());
        assertEquals(
                new BigDecimal("2.1"),
                response.getAppliedInterestRate().getAnnualRate()
        );
        assertTrue(response.getDisclaimer().contains("기본금리"));
    }

    @Test
    void rejectsMaturityEstimateOutsideProductConditions() {
        FinancialProduct product = product(1L);
        product.setContractPeriodMonths(12);
        when(financialProductMapper.findActiveProductById(1L))
                .thenReturn(product);

        BusinessException amountException = assertThrows(
                BusinessException.class,
                () -> financialProductService.estimateMaturity(
                        1L,
                        new FinancialProductMaturityEstimateRequest(
                                new BigDecimal("9999"),
                                12
                        )
                )
        );
        BusinessException periodException = assertThrows(
                BusinessException.class,
                () -> financialProductService.estimateMaturity(
                        1L,
                        new FinancialProductMaturityEstimateRequest(
                                new BigDecimal("100000"),
                                6
                        )
                )
        );

        assertEquals(
                ErrorCode.INVALID_MATURITY_ESTIMATE_REQUEST,
                amountException.getErrorCode()
        );
        assertEquals(
                ErrorCode.INVALID_MATURITY_ESTIMATE_REQUEST,
                periodException.getErrorCode()
        );
    }

    @Test
    void rejectsUnsupportedMaturityEstimateProduct() {
        FinancialProduct product = product(1L);
        product.setProductType("ACCOUNT");
        when(financialProductMapper.findActiveProductById(1L))
                .thenReturn(product);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> financialProductService.estimateMaturity(
                        1L,
                        new FinancialProductMaturityEstimateRequest(
                                new BigDecimal("100000"),
                                12
                        )
                )
        );

        assertEquals(
                ErrorCode.MATURITY_ESTIMATE_NOT_AVAILABLE,
                exception.getErrorCode()
        );
    }

    private FinancialProduct product(long productId) {
        FinancialProduct product = new FinancialProduct();
        product.setFinancialProductId(productId);
        product.setProductType("SAVING");
        product.setTargetOwnerType("CHILD");
        product.setName("Product " + productId);
        product.setProductSubtype("자유적립식 예금");
        product.setHighlightLabel("깨비 추천");
        product.setCurationReason("정적 큐레이션 사유");
        product.setDisplayBadgesJson("[\"#만19세미만\",\"#자유적립\"]");
        product.setBaseInterestRate(new BigDecimal("2.1"));
        product.setMaxInterestRate(new BigDecimal("3.4"));
        product.setInterestRateReference("12개월 기준 · 세금공제 전");
        product.setMinAge(0);
        product.setMaxAge(19);
        product.setMinMonthlyAmount(new BigDecimal("10000"));
        product.setMaxMonthlyAmount(new BigDecimal("3000000"));
        product.setMinContractPeriodMonths(12);
        product.setMaxContractPeriodMonths(12);
        product.setInterestPaymentMethod("MATURITY_LUMP_SUM");
        product.setJoinTerminationMethod("Mock 가입·해지 안내");
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
