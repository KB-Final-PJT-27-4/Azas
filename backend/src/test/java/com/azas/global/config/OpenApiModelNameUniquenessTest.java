package com.azas.global.config;

import io.swagger.annotations.ApiModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiModelNameUniquenessTest {

    @Test
    void duplicateJavaTypeNamesUseUniqueOpenApiModelNames() {
        List<List<Class<?>>> duplicateTypeGroups = List.of(
                List.of(
                        com.azas.domain.allowance.dto.AllowanceRequestResponse.class,
                        com.azas.domain.family.dto.AllowanceRequestResponse.class
                ),
                List.of(
                        com.azas.domain.checklist.dto.ChecklistItemListResponse.Item.class,
                        com.azas.domain.finance.goal.dto.FinancialGoalTemplateListResponse.Item.class,
                        com.azas.domain.finance.product.dto.FinancialProductBookmarkListResponse.Item.class,
                        com.azas.domain.finance.product.dto.FinancialProductListResponse.Item.class,
                        com.azas.domain.notification.dto.UpdateNotificationPreferencesRequest.Item.class
                ),
                List.of(
                        com.azas.domain.dashboard.dto.ChildDashboardResponse.ChildInfo.class,
                        com.azas.domain.dashboard.dto.ParentDashboardResponse.ChildInfo.class
                ),
                List.of(
                        com.azas.domain.dashboard.dto.ChildDashboardResponse.NotificationSummary.class,
                        com.azas.domain.dashboard.dto.ParentDashboardResponse.NotificationSummary.class
                ),
                List.of(
                        com.azas.domain.finance.account.dto.LinkedAccountResponse.class,
                        com.azas.domain.finance.goal.dto.FinancialGoalCreateResponse.LinkedAccountResponse.class,
                        com.azas.domain.finance.goal.dto.FinancialGoalDetailResponse.LinkedAccountResponse.class,
                        com.azas.domain.finance.goal.dto.FinancialGoalListResponse.LinkedAccountResponse.class
                ),
                List.of(
                        com.azas.domain.finance.product.dto.FinancialProductDetailResponse.ContractPeriod.class,
                        com.azas.domain.finance.product.dto.FinancialProductListResponse.ContractPeriod.class
                ),
                List.of(
                        com.azas.domain.report.dto.AssetReportDetailResponse.Period.class,
                        com.azas.domain.report.dto.ChildcareReportDetailResponse.Period.class
                ),
                List.of(
                        com.azas.domain.report.dto.AssetReportDetailResponse.Summary.class,
                        com.azas.domain.report.dto.ChildcareReportDetailResponse.Summary.class
                )
        );

        for (List<Class<?>> group : duplicateTypeGroups) {
            Set<String> modelNames = group.stream()
                    .map(this::openApiModelName)
                    .collect(Collectors.toSet());

            assertEquals(
                    group.size(),
                    modelNames.size(),
                    "동일 Java 타입명의 OpenAPI 모델명이 중복되었습니다: "
                            + group.get(0).getSimpleName()
            );
        }
    }

    private String openApiModelName(Class<?> type) {
        ApiModel apiModel = type.getAnnotation(ApiModel.class);
        assertNotNull(
                apiModel,
                type.getName() + "에 @ApiModel이 필요합니다."
        );
        assertFalse(
                apiModel.value().isBlank(),
                type.getName() + "의 @ApiModel value가 필요합니다."
        );
        return apiModel.value();
    }
}
