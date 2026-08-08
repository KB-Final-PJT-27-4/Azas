package com.azas.domain.finance.transfer.service;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountNumberProtectorTest {

    @Test
    void 계좌번호를_암호화한_뒤_복호화한다() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);

        String keyBase64 =
                Base64.getEncoder().encodeToString(keyBytes);

        AccountNumberProtector protector =
                new AccountNumberProtector(
                        keyBase64,
                        new SecureRandom()
                );

        String accountNumber = "987-6543-54321";

        byte[] encryptedAccountNumber =
                protector.encrypt(accountNumber);

        String decryptedAccountNumber =
                protector.decrypt(encryptedAccountNumber);

        assertEquals(accountNumber, decryptedAccountNumber);
    }
}