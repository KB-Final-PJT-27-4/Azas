package com.azas.domain.family.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class FamilyMapperXmlTest {


    @Test
    void parsesFamilyMapperXml() {
        assertDoesNotThrow(() -> parse("mapper/family/FamilyMapper.xml"));
    }

    private void parse(String resourcePath) throws Exception {
        Configuration configuration = new Configuration();

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
    }
}
