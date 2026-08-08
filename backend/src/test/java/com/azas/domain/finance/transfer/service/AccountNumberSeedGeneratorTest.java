package com.azas.domain.finance.transfer.service;

import org.junit.jupiter.api.Test;

import java.util.Base64;

class AccountNumberSeedGeneratorTest {

    @Test
    void seed용_계좌번호_암호문을_출력한다() {
        String encryptionKeyBase64 = System.getenv(
                "ACCOUNT_NUMBER_ENCRYPTION_KEY_BASE64"
        );

        AccountNumberProtector protector =
                new AccountNumberProtector(
                        encryptionKeyBase64,
                        new java.security.SecureRandom()
                );

        printCiphertext(protector, "987-6543-5678");
        printCiphertext(protector, "123-4567-1001");
        printCiphertext(protector, "123-4567-2001");
    }

    private void printCiphertext(
            AccountNumberProtector protector,
            String accountNumber
    ) {
        String ciphertextBase64 = Base64.getEncoder()
                .encodeToString(protector.encrypt(accountNumber));

        System.out.println(
                accountNumber + " -> " + ciphertextBase64
        );
    }
}