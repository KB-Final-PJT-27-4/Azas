package com.azas.domain.finance.goal.mapper;

import com.azas.domain.finance.goal.entity.FinancialGoalAmountRecommendation;
import com.azas.domain.finance.goal.entity.FinancialGoalRecommendationBasis;
import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
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
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(
        named = "RUN_MYSQL_INTEGRATION_TESTS",
        matches = "true"
)
class FinancialGoalTemplateMapperIntegrationTest {

    @Test
    void readsRecommendationBasisAndFourStagesFromMySql()
            throws Exception {
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
        configuration.setMapUnderscoreToCamelCase(true);
        String resourcePath =
                "mapper/finance/goal/FinancialGoalTemplateMapper.xml";

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
            FinancialGoalTemplateMapper mapper =
                    session.getMapper(FinancialGoalTemplateMapper.class);

            FinancialGoalTemplate template =
                    mapper.findActiveDefaultTemplateById(1L);
            FinancialGoalRecommendationBasis basis =
                    mapper.findRecommendationBasisByTemplateId(1L);
            List<FinancialGoalAmountRecommendation> recommendations =
                    mapper.findActiveAmountRecommendationsByTemplateId(1L);

            assertNotNull(template);
            assertEquals("대학자금", template.getGoalName());
            assertNotNull(basis);
            assertEquals(
                    new BigDecimal("7106500.00"),
                    basis.getMetricValue()
            );
            assertEquals(4, recommendations.size());
            assertEquals(
                    "STARTER",
                    recommendations.get(0).getRecommendationCode()
            );
            assertEquals(
                    new BigDecimal("100000000.00"),
                    recommendations.get(3).getTargetAmount()
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
