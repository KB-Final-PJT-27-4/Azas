package com.azas.domain.child.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class PregnancyDateProvider {

    private static final ZoneId KOREA_ZONE =
            ZoneId.of("Asia/Seoul");

    public LocalDate today() {
        return LocalDate.now(KOREA_ZONE);
    }
}