package com.azas.domain.finance.product.mapper;

import com.azas.domain.finance.product.entity.FinancialProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinancialProductMapper {

    int countActiveChild(@Param("childId") long childId);

    int countChildAccess(
            @Param("childId") long childId,
            @Param("memberId") long memberId
    );

    FinancialProduct findActiveProductById(
            @Param("financialProductId") long financialProductId
    );

    List<FinancialProduct> findActiveProducts(
            @Param("productType") String productType,
            @Param("lastProductId") Long lastProductId,
            @Param("limit") int limit
    );

    int countBookmark(
            @Param("memberId") long memberId,
            @Param("childId") long childId,
            @Param("financialProductId") long financialProductId
    );

    int insertBookmarkIfAbsent(
            @Param("memberId") long memberId,
            @Param("childId") long childId,
            @Param("financialProductId") long financialProductId
    );

    int deleteBookmark(
            @Param("memberId") long memberId,
            @Param("childId") long childId,
            @Param("financialProductId") long financialProductId
    );

    long countBookmarks(
            @Param("memberId") long memberId,
            @Param("childId") long childId,
            @Param("productType") String productType
    );

    List<FinancialProduct> findBookmarks(
            @Param("memberId") long memberId,
            @Param("childId") long childId,
            @Param("productType") String productType,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
