package com.azas.domain.finance.account.service;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class MockAccountNumberGenerator {
    private final SecureRandom random = new SecureRandom();
    public String generate() {
        return "%03d-%03d-%06d".formatted(
                random.nextInt(1000), random.nextInt(1000),
                random.nextInt(1_000_000));
    }
}
