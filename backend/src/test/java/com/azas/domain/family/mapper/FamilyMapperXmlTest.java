package com.azas.domain.family.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FamilyMapperXmlTest {


    @Test
    void parsesFamilyMapperXml() {
        assertDoesNotThrow(() -> parse("mapper/family/FamilyMapper.xml"));
    }

    @Test
    void castsInvitationChildIdsToSignedValuesForLongDtoConstructor() throws Exception {
        String resourcePath = "mapper/family/FamilyMapper.xml";

        try (InputStream inputStream = Resources.getResourceAsStream(resourcePath)) {
            String mapperXml = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            assertTrue(
                    countOccurrences(
                            mapperXml,
                            "CAST(c.child_id AS SIGNED) AS childId"
                    ) >= 2
            );
        }
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

    private int countOccurrences(String source, String target) {
        return source.split(java.util.regex.Pattern.quote(target), -1).length - 1;
    }
}
