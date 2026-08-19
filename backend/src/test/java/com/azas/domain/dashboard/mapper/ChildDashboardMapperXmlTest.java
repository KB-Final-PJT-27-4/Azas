package com.azas.domain.dashboard.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChildDashboardMapperXmlTest {

    private static final String RESOURCE_PATH =
            "mapper/dashboard/ChildDashboardMapper.xml";
    private static final String NAMESPACE =
            "com.azas.domain.dashboard.mapper.ChildDashboardMapper.";

    @Test
    void mapperXml을_파싱하고_모든_조회문을_등록한다() {
        Configuration configuration = new Configuration();

        assertDoesNotThrow(() -> {
            try (InputStream inputStream =
                         Resources.getResourceAsStream(RESOURCE_PATH)) {
                new XMLMapperBuilder(
                        inputStream,
                        configuration,
                        RESOURCE_PATH,
                        configuration.getSqlFragments()
                ).parse();
            }
        });

        assertTrue(configuration.hasStatement(
                NAMESPACE + "findActiveChildByMemberId"
        ));
        assertTrue(configuration.hasStatement(
                NAMESPACE + "findPrimaryAccountUsage"
        ));
        assertTrue(configuration.hasStatement(
                NAMESPACE + "findActivitySummary"
        ));
        assertTrue(configuration.hasStatement(
                NAMESPACE + "countActiveMissions"
        ));
        assertTrue(configuration.hasStatement(
                NAMESPACE + "findMissionPreview"
        ));
        assertTrue(configuration.hasStatement(
                NAMESPACE + "countUnreadNotifications"
        ));
    }
}
