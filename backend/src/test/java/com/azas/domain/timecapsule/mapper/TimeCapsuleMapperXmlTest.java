package com.azas.domain.timecapsule.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TimeCapsuleMapperXmlTest {

    @Test
    // [JMG] CAPSULE-1~8 타임캡슐 MyBatis 매퍼가 애플리케이션 시작 전에 정상 파싱되는지 검증한다.
    void parsesTimeCapsuleMapperXmlFiles() {
        assertDoesNotThrow(() -> parse("mapper/timecapsule/TimeCapsuleMapper.xml"));
        assertDoesNotThrow(
                () -> parse("mapper/timecapsule/TimeCapsuleEntryMapper.xml")
        );
        assertDoesNotThrow(
                () -> parse("mapper/timecapsule/TimeCapsuleMediaMapper.xml")
        );
    }

    // [JMG] CAPSULE-1~8 매퍼 XML 하나를 독립 MyBatis 설정으로 파싱해 SQL·결과 매핑 오류를 조기에 찾는다.
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
