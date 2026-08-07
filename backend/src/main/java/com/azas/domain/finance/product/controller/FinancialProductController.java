package com.azas.domain.finance.product.controller;

import com.azas.domain.finance.product.dto.FinancialProductBookmarkListResponse;
import com.azas.domain.finance.product.dto.FinancialProductBookmarkRequest;
import com.azas.domain.finance.product.dto.FinancialProductBookmarkResponse;
import com.azas.domain.finance.product.dto.FinancialProductDetailResponse;
import com.azas.domain.finance.product.dto.FinancialProductRecommendationResponse;
import com.azas.domain.finance.product.service.FinancialProductService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;

@Api(tags = "Financial products")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FinancialProductController {

    private final AccessTokenMemberResolver accessTokenMemberResolver;
    private final FinancialProductService financialProductService;

    @ApiOperation("PRODUCT-1 Financial product recommendations")
    @GetMapping("/children/{child_id}/financial-products/recommendations")
    public ResponseEntity<FinancialProductRecommendationResponse>
    getRecommendations(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") long childId,
            @RequestParam(value = "account_id", required = false)
            Long financialAccountId,
            @RequestParam(value = "monthly_amount", required = false)
            BigDecimal monthlyAmount,
            @RequestParam(value = "product_type", required = false)
            String productType,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        return ResponseEntity.ok(financialProductService.getRecommendations(
                memberId,
                childId,
                financialAccountId,
                monthlyAmount,
                productType,
                cursor,
                size
        ));
    }

    @ApiOperation("PRODUCT-2 Financial product bookmarks")
    @GetMapping("/children/{child_id}/financial-products/bookmarks")
    public ResponseEntity<FinancialProductBookmarkListResponse> getBookmarks(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") long childId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "product_type", required = false)
            String productType
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        return ResponseEntity.ok(financialProductService.getBookmarks(
                memberId,
                childId,
                page,
                size,
                productType
        ));
    }

    @ApiOperation("PRODUCT-3 Update financial product bookmark")
    @PutMapping("/children/{child_id}/financial-products/{financial_product_id}/bookmark")
    public ResponseEntity<FinancialProductBookmarkResponse> updateBookmark(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") long childId,
            @PathVariable("financial_product_id") long financialProductId,
            @Valid @RequestBody FinancialProductBookmarkRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        return ResponseEntity.ok(financialProductService.updateBookmark(
                memberId,
                childId,
                financialProductId,
                request.getBookmarked()
        ));
    }

    @ApiOperation("PRODUCT-4 Financial product detail")
    @GetMapping("/financial-products/{financial_product_id}")
    public ResponseEntity<FinancialProductDetailResponse> getProductDetail(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("financial_product_id") long financialProductId,
            @RequestParam(value = "child_id", required = false) Long childId,
            @RequestParam(value = "account_id", required = false)
            Long financialAccountId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        return ResponseEntity.ok(financialProductService.getProductDetail(
                memberId,
                financialProductId,
                childId,
                financialAccountId
        ));
    }
}
