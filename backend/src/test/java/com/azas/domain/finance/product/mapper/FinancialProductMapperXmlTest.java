package com.azas.domain.finance.product.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
}
