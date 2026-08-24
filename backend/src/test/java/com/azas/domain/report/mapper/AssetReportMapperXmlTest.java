package com.azas.domain.report.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetReportMapperXmlTest {

    private static final String RESOURCE_PATH =
            "mapper/report/AssetReportMapper.xml";

    private static final String NAMESPACE =
            "com.azas.domain.report.mapper.AssetReportMapper.";

    @Test
    void 자산_리포트_Mapper_XML을_파싱한다() {
        Configuration configuration =
                new Configuration();

        assertDoesNotThrow(() -> {
            try (InputStream inputStream =
                         Resources.getResourceAsStream(
                                 RESOURCE_PATH
                         )) {
                new XMLMapperBuilder(
                        inputStream,
                        configuration,
                        RESOURCE_PATH,
                        configuration.getSqlFragments()
                ).parse();
            }
        });

        assertTrue(
                configuration.hasStatement(
                        NAMESPACE + "findActiveChildId"
                )
        );

        assertTrue(
                configuration.hasStatement(
                        NAMESPACE + "countParentAccess"
                )
        );

        assertTrue(
                configuration.hasStatement(
                        NAMESPACE + "findAssetReports"
                )
        );
        assertTrue(
                configuration.hasStatement(
                        NAMESPACE + "findAssetReportDetail"
                )
        );
        assertTrue(
                configuration.hasStatement(
                        NAMESPACE + "findGoalAccountSnapshots"
                )
        );
    }

    @Test
    void childAssetReportAggregatesAllChildLinkedSavingsAccounts()
            throws Exception {
        String mapperXml;

        try (InputStream inputStream =
                     Resources.getResourceAsStream(RESOURCE_PATH)) {
            mapperXml = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }

        assertTrue(
                mapperXml.contains(
                        "AND fa.account_product_type = 'SAVINGS'"
                )
        );
        assertTrue(
                mapperXml.contains(
                        "ON fa.financial_account_id = at.financial_account_id"
                )
        );
        assertTrue(
                mapperXml.contains(
                        "AND fa.child_id = #{childId}"
                )
        );
        assertTrue(
                !mapperXml.contains("fa.owner_type = 'CHILD'")
        );
    }
}
