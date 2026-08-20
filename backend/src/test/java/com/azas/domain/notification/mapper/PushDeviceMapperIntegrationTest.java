package com.azas.domain.notification.mapper;

import com.azas.domain.notification.dto.PushDeviceCommand;
import com.azas.domain.notification.dto.PushDeviceRow;
import com.azas.domain.notification.entity.PushPlatform;
import com.azas.domain.notification.entity.PushProvider;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(
        named = "RUN_MYSQL_INTEGRATION_TESTS",
        matches = "true"
)
class PushDeviceMapperIntegrationTest {

    @Test
    void executesPushDeviceSqlAgainstMySql() throws Exception {
        PooledDataSource dataSource = new PooledDataSource(
                "com.mysql.cj.jdbc.Driver",
                requireEnvironment("DB_URL"),
                requireEnvironment("DB_USERNAME"),
                requireEnvironment("DB_PASSWORD")
        );

        Environment environment = new Environment(
                "mysql-integration-test",
                new JdbcTransactionFactory(),
                dataSource
        );
        Configuration configuration = new Configuration(environment);
        String resourcePath =
                "mapper/notification/PushDeviceMapper.xml";

        try (InputStream inputStream =
                     Resources.getResourceAsStream(resourcePath)) {
            new XMLMapperBuilder(
                    inputStream,
                    configuration,
                    resourcePath,
                    configuration.getSqlFragments()
            ).parse();
        }

        SqlSessionFactory sessionFactory =
                new SqlSessionFactoryBuilder().build(configuration);

        try (SqlSession session = sessionFactory.openSession(false)) {
            PushDeviceMapper mapper =
                    session.getMapper(PushDeviceMapper.class);
            String deviceKey = "push-device-integration-test";

            int affectedRows = mapper.upsert(new PushDeviceCommand(
                    1L,
                    deviceKey,
                    PushPlatform.WEB,
                    PushProvider.FCM,
                    "Integration Test",
                    new byte[]{1, 2, 3},
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            ));

            PushDeviceRow row = mapper.findByMemberAndDeviceKey(
                    1L,
                    deviceKey
            );

            assertTrue(affectedRows >= 1);
            assertNotNull(row);
            assertEquals(PushPlatform.WEB, row.getPlatform());
            assertTrue(row.isActive());
            session.rollback();
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
