package com.azas.domain.finance.product.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialProductMapperXmlTest {

    @Test
    void parsesFinancialProductMapperXml() {
        assertDoesNotThrow(() -> {
            Configuration configuration = new Configuration();
            String resourcePath = "mapper/finance/product/FinancialProductMapper.xml";

            try (InputStream inputStream = Resources.getResourceAsStream(
                    resourcePath
            )) {
                new XMLMapperBuilder(
                        inputStream,
                        configuration,
                        resourcePath,
                        configuration.getSqlFragments()
                ).parse();
            }
        });
    }

    @Test
    void childProductQueriesExcludeParentOnlyProducts() throws Exception {
        String resourcePath = "mapper/finance/product/FinancialProductMapper.xml";
        String filter = "target_owner_type IN ('CHILD', 'BOTH')";

        try (InputStream inputStream = Resources.getResourceAsStream(
                resourcePath
        )) {
            String xml = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            assertTrue(xml.contains("target_owner_type,"));
            assertTrue(xml.contains("p.target_owner_type,"));
            assertEquals(3, xml.split(
                    java.util.regex.Pattern.quote(filter), -1
            ).length - 1);
        }
    }
}
