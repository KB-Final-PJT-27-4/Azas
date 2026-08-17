package com.azas.domain.child.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ChildMapperXmlTest {

    @Test
    void parsesChildMapperXml() {
        assertDoesNotThrow(() -> {
            Configuration configuration =
                    new Configuration();

            String resourcePath =
                    "mapper/child/ChildMapper.xml";

            try (
                    InputStream inputStream =
                            Resources.getResourceAsStream(
                                    resourcePath
                            )
            ) {
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