package com.azas.domain.report.mapper;

import com.azas.domain.report.dto.AssetReportListQuery;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.InputStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(
        named = "RUN_MYSQL_INTEGRATION_TESTS",
        matches = "true"
)
class AssetReportMapperIntegrationTest {

    @Test
    void 실제_MySQL에서_자산_리포트_SQL을_실행한다()
            throws Exception {

        String dbUrl = requireEnvironment("DB_URL");
        String dbUsername =
                requireEnvironment("DB_USERNAME");
        String dbPassword =
                requireEnvironment("DB_PASSWORD");

        PooledDataSource dataSource =
                new PooledDataSource(
                        "com.mysql.cj.jdbc.Driver",
                        dbUrl,
                        dbUsername,
                        dbPassword
                );

        Environment environment =
                new Environment(
                        "mysql-integration-test",
                        new JdbcTransactionFactory(),
                        dataSource
                );

        Configuration configuration =
                new Configuration(environment);

        String resourcePath =
                "mapper/report/AssetReportMapper.xml";

        try (InputStream inputStream =
                     Resources.getResourceAsStream(
                             resourcePath
                     )) {
            new XMLMapperBuilder(
                    inputStream,
                    configuration,
                    resourcePath,
                    configuration.getSqlFragments()
            ).parse();
        }

        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder()
                        .build(configuration);

        try (SqlSession sqlSession =
                     sqlSessionFactory.openSession(true)) {

            AssetReportMapper mapper =
                    sqlSession.getMapper(
                            AssetReportMapper.class
                    );

            AssetReportListQuery query =
                    new AssetReportListQuery(
                            1L,
                            null,
                            null,
                            null,
                            null,
                            1
                    );

            assertDoesNotThrow(() -> {
                assertNotNull(
                        mapper.findAssetReports(query)
                );
            });

            assertDoesNotThrow(() ->
                    mapper.findAssetReportDetail(
                            1L,
                            LocalDate.of(2026, 7, 1)
                    )
            );

        } finally {
            dataSource.forceCloseAll();
        }
    }

    private String requireEnvironment(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    name + " 환경변수가 필요합니다."
            );
        }

        return value;
    }
}
