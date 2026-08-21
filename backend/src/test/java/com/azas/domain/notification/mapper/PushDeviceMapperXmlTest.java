package com.azas.domain.notification.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PushDeviceMapperXmlTest {

    @Test
    void parsesPushDeviceMapperXml() {
        assertDoesNotThrow(() -> {
            Configuration configuration = new Configuration();
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
        });
    }
}
