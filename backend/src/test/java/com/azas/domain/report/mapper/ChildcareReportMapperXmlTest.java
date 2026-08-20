package com.azas.domain.report.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChildcareReportMapperXmlTest {

    private static final String RESOURCE_PATH =
            "mapper/report/ChildcareReportMapper.xml";

    private static final String NAMESPACE =
            "com.azas.domain.report.mapper.ChildcareReportMapper.";

    @Test
    void 양육비_리포트_Mapper_XML을_파싱한다() {
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
                        NAMESPACE + "findMonthlyExpenses"
                )
        );
    }
}