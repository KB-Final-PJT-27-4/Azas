package com.azas.domain.finance.product.service;

import com.azas.domain.finance.product.dto.FinancialProductBookmarkListResponse;
import com.azas.domain.finance.product.dto.FinancialProductBookmarkResponse;
import com.azas.domain.finance.product.dto.FinancialProductDetailResponse;
import com.azas.domain.finance.product.dto.FinancialProductListResponse;
import com.azas.domain.finance.product.entity.FinancialProduct;
import com.azas.domain.finance.product.mapper.FinancialProductMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FinancialProductService {

    private static final int DEFAULT_PRODUCT_LIST_SIZE = 20;
    private static final int DEFAULT_BOOKMARK_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> PRODUCT_TYPES = Set.of(
            "SAVING", "DEPOSIT", "ACCOUNT", "CARD", "SUBSCRIPTION"
    );

    private final FinancialProductMapper financialProductMapper;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public FinancialProductListResponse getProducts(
            String productType,
            String cursor,
            Integer size
    ) {
        String normalizedProductType = normalizeProductType(productType);
        Long lastProductId = parseCursor(cursor);
        int pageSize = normalizeSize(size, DEFAULT_PRODUCT_LIST_SIZE);

        List<FinancialProduct> candidates =
                financialProductMapper.findActiveProducts(
                        normalizedProductType,
                        lastProductId,
                        pageSize + 1
                );
        boolean hasNext = candidates.size() > pageSize;
        List<FinancialProduct> pageItems = hasNext
                ? new ArrayList<>(candidates.subList(0, pageSize))
                : candidates;
        String nextCursor = hasNext
                ? String.valueOf(pageItems.get(pageItems.size() - 1)
                .getFinancialProductId())
                : null;

        List<FinancialProductListResponse.Item> items = new ArrayList<>();
        for (FinancialProduct product : pageItems) {
            items.add(FinancialProductListResponse.Item.from(
                    product,
                    parseDisplayBadges(product.getDisplayBadgesJson())
            ));
        }

        return new FinancialProductListResponse(items, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public FinancialProductBookmarkListResponse getBookmarks(
            long requesterMemberId,
            long childId,
            Integer page,
            Integer size,
            String productType
    ) {
        assertChildAccess(requesterMemberId, childId);

        int normalizedPage = normalizePage(page);
        int pageSize = normalizeSize(size, DEFAULT_BOOKMARK_SIZE);
        String normalizedProductType = normalizeProductType(productType);
        long totalElements = financialProductMapper.countBookmarks(
                requesterMemberId,
                childId,
                normalizedProductType
        );
        List<FinancialProduct> products = financialProductMapper.findBookmarks(
                requesterMemberId,
                childId,
                normalizedProductType,
                pageSize,
                normalizedPage * pageSize
        );

        return new FinancialProductBookmarkListResponse(
                products,
                normalizedPage,
                pageSize,
                totalElements
        );
    }

    @Transactional
    public FinancialProductBookmarkResponse updateBookmark(
            long requesterMemberId,
            long childId,
            long financialProductId,
            boolean bookmarked
    ) {
        assertChildAccess(requesterMemberId, childId);
        FinancialProduct product = getActiveProductOrThrow(financialProductId);
        assertChildEligibleProduct(product);

        if (bookmarked) {
            financialProductMapper.insertBookmarkIfAbsent(
                    requesterMemberId,
                    childId,
                    financialProductId
            );
        } else {
            financialProductMapper.deleteBookmark(
                    requesterMemberId,
                    childId,
                    financialProductId
            );
        }

        return new FinancialProductBookmarkResponse(
                childId,
                financialProductId,
                bookmarked
        );
    }

    @Transactional(readOnly = true)
    public FinancialProductDetailResponse getProductDetail(
            long requesterMemberId,
            long financialProductId,
            Long childId
    ) {
        if (financialProductId <= 0 || (childId != null && childId <= 0)) {
            throw new BusinessException(ErrorCode.INVALID_QUERY_PARAMETER);
        }
        FinancialProduct product = getActiveProductOrThrow(financialProductId);
        boolean bookmarked = false;

        if (childId != null) {
            assertChildAccess(requesterMemberId, childId);
            if (isChildEligibleProduct(product)) {
                bookmarked = financialProductMapper.countBookmark(
                        requesterMemberId,
                        childId,
                        financialProductId
                ) > 0;
            }
        }

        return FinancialProductDetailResponse.from(
                product,
                bookmarked,
                objectMapper
        );
    }

    private FinancialProduct getActiveProductOrThrow(long financialProductId) {
        FinancialProduct product = financialProductMapper
                .findActiveProductById(financialProductId);
        if (product == null) {
            throw new BusinessException(ErrorCode.FINANCIAL_PRODUCT_NOT_FOUND);
        }
        return product;
    }

    private void assertChildEligibleProduct(FinancialProduct product) {
        if (!isChildEligibleProduct(product)) {
            throw new BusinessException(ErrorCode.FINANCIAL_PRODUCT_NOT_FOUND);
        }
    }

    private boolean isChildEligibleProduct(FinancialProduct product) {
        return "CHILD".equals(product.getTargetOwnerType())
                || "BOTH".equals(product.getTargetOwnerType());
    }

    private void assertChildAccess(long requesterMemberId, long childId) {
        if (financialProductMapper.countActiveChild(childId) == 0) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
        if (financialProductMapper.countChildAccess(
                childId,
                requesterMemberId
        ) == 0) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }
    }

    private String normalizeProductType(String productType) {
        if (productType == null || productType.isBlank()) {
            return null;
        }
        String normalized = productType.trim().toUpperCase(Locale.ROOT);
        if (!PRODUCT_TYPES.contains(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_QUERY_PARAMETER);
        }
        return normalized;
    }

    private Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            long productId = Long.parseLong(cursor);
            if (productId <= 0) {
                throw new NumberFormatException();
            }
            return productId;
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.INVALID_QUERY_PARAMETER);
        }
    }

    private int normalizeSize(Integer size, int defaultSize) {
        int normalized = size == null ? defaultSize : size;
        if (normalized < 1 || normalized > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_QUERY_PARAMETER);
        }
        return normalized;
    }

    private int normalizePage(Integer page) {
        int normalized = page == null ? 0 : page;
        if (normalized < 0) {
            throw new BusinessException(ErrorCode.INVALID_QUERY_PARAMETER);
        }
        return normalized;
    }

    private List<String> parseDisplayBadges(String displayBadgesJson) {
        if (displayBadgesJson == null || displayBadgesJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    displayBadgesJson,
                    new TypeReference<List<String>>() { }
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
