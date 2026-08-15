package com.azas.domain.finance.product.controller;

import com.azas.domain.finance.product.dto.FinancialProductBookmarkListResponse;
import com.azas.domain.finance.product.dto.FinancialProductBookmarkRequest;
import com.azas.domain.finance.product.dto.FinancialProductBookmarkResponse;
import com.azas.domain.finance.product.dto.FinancialProductDetailResponse;
import com.azas.domain.finance.product.dto.FinancialProductListResponse;
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

@Api(
        tags = "금융상품",
        description = "금융상품 조회 및 관심상품 API"
)
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FinancialProductController {

    private final AccessTokenMemberResolver accessTokenMemberResolver;
    private final FinancialProductService financialProductService;

    @ApiOperation("PRODUCT-1 KB 금융상품 목록 조회")
    @GetMapping("/financial-products")
    public ResponseEntity<FinancialProductListResponse> getProducts(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @RequestParam(value = "product_type", required = false)
            String productType,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        accessTokenMemberResolver.resolveMemberId(authorizationHeader);
        return ResponseEntity.ok(financialProductService.getProducts(
                productType,
                cursor,
                size
        ));
    }

    @ApiOperation("PRODUCT-2 자녀별 관심상품 목록 조회")
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

    @ApiOperation("PRODUCT-3 자녀별 관심상품 저장·해제")
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

    @ApiOperation("PRODUCT-4 금융상품 상세 조회")
    @GetMapping("/financial-products/{financial_product_id}")
    public ResponseEntity<FinancialProductDetailResponse> getProductDetail(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("financial_product_id") long financialProductId,
            @RequestParam(value = "child_id", required = false) Long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        return ResponseEntity.ok(financialProductService.getProductDetail(
                memberId,
                financialProductId,
                childId
        ));
    }
}
