package com.azas.domain.notification.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ProactiveNotificationMapperXmlTest {
    @Test
    void proactiveNotificationMapperXml을_파싱한다() {
        assertDoesNotThrow(() -> {
            Configuration configuration = new Configuration();
            try (InputStream inputStream = Resources.getResourceAsStream(
                    "mapper/notification/ProactiveNotificationMapper.xml")) {
                new XMLMapperBuilder(inputStream, configuration,
                        "mapper/notification/ProactiveNotificationMapper.xml",
                        configuration.getSqlFragments()).parse();
            }
        });
    }
}
