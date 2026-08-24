package com.azas.domain.dashboard.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;

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
                NAMESPACE + "findPreferredAccountUsage"
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

    @Test
    void 아이_입출금계좌는_주계좌가_없어도_최근_활성계좌로_조회한다() {
        Configuration configuration = parseMapper();

        BoundSql boundSql = configuration.getMappedStatement(
                        NAMESPACE + "findPreferredAccountUsage"
                )
                .getBoundSql(Map.of(
                        "childId", 6L,
                        "startOccurredAt", LocalDateTime.of(2026, 8, 1, 0, 0),
                        "endOccurredAtExclusive", LocalDateTime.of(2026, 9, 1, 0, 0)
                ));
        String sql = boundSql.getSql().replaceAll("\\s+", " ");

        assertTrue(sql.contains("ORDER BY fa.is_primary DESC, fa.linked_at DESC"));
        assertTrue(!sql.contains("AND fa.is_primary = 1"));
    }

    private Configuration parseMapper() {
        Configuration configuration = new Configuration();

        try (InputStream inputStream =
                     Resources.getResourceAsStream(RESOURCE_PATH)) {
            new XMLMapperBuilder(
                    inputStream,
                    configuration,
                    RESOURCE_PATH,
                    configuration.getSqlFragments()
            ).parse();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }

        return configuration;
    }
}
